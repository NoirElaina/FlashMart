<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Clock, CreditCard, PackageX, ReceiptText, RotateCcw, Truck } from 'lucide-vue-next'
import Navbar from '@/layout/Navbar.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { cancelOrder, getOrderDetail, payOrder } from '@/api/order'
import { resolveApiError } from '@/utils/apiError'
import { formatOrderCountdown, getOrderPaymentRemainingSeconds } from '@/utils/orderCountdown'
import type { OrderDetail } from '@/types/order'

const route = useRoute()
const router = useRouter()

const order = ref<OrderDetail | null>(null)
const loading = ref(false)
const canceling = ref(false)
const paying = ref(false)
const errorMessage = ref('')
const now = ref(Date.now())
const timeoutRefreshTriggered = ref(false)
let countdownTimer: number | undefined

const orderId = computed(() => Number(route.params.id))
const isPendingPayment = computed(() => order.value?.status === 'PENDING_PAYMENT')
const canCancel = computed(() => isPendingPayment.value)
const paymentRemainingSeconds = computed(() => {
    if (!order.value || !isPendingPayment.value) {
        return null
    }

    return getOrderPaymentRemainingSeconds(order.value.payExpireTime, now.value)
})
const canPay = computed(() => isPendingPayment.value && (paymentRemainingSeconds.value ?? 0) > 0)
const paymentCountdownText = computed(() => {
    if (paymentRemainingSeconds.value === null) {
        return ''
    }

    if (paymentRemainingSeconds.value === 0) {
        return '订单已超时，状态更新中'
    }

    return `剩余 ${formatOrderCountdown(paymentRemainingSeconds.value)} 支付`
})

function formatPrice(price: number) {
    return price.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    })
}

function resolveStatusText(status: string) {
    const statusMap: Record<string, string> = {
        PENDING_PAYMENT: '待支付',
        PAID: '已支付',
        CANCELED: '已取消',
        FINISHED: '已完成',
    }

    return statusMap[status] ?? status
}

async function fetchOrderDetail() {
    if (!Number.isInteger(orderId.value) || orderId.value <= 0) {
        errorMessage.value = '订单编号不合法'
        return
    }

    loading.value = true
    errorMessage.value = ''

    try {
        const response = await getOrderDetail(orderId.value)
        if (response.data.code !== 200) {
            errorMessage.value = response.data.message || '订单详情加载失败'
            return
        }

        order.value = response.data.data
        if (order.value.status !== 'PENDING_PAYMENT') {
            timeoutRefreshTriggered.value = false
        }
    } catch (error) {
        errorMessage.value = resolveApiError(error, '订单详情加载失败，请稍后重试')
    } finally {
        loading.value = false
    }
}

async function handleCancelOrder() {
    if (!order.value || !canCancel.value || canceling.value || paying.value) {
        return
    }

    canceling.value = true
    errorMessage.value = ''

    try {
        const response = await cancelOrder(order.value.orderId)
        if (response.data.code !== 200) {
            errorMessage.value = response.data.message || '取消订单失败'
            return
        }

        await fetchOrderDetail()
    } catch (error) {
        errorMessage.value = resolveApiError(error, '取消订单失败，请稍后重试')
    } finally {
        canceling.value = false
    }
}

async function handlePayOrder() {
    if (!order.value || !canPay.value || paying.value || canceling.value) {
        return
    }

    paying.value = true
    errorMessage.value = ''

    try {
        const response = await payOrder(order.value.orderId)
        if (response.data.code !== 200) {
            errorMessage.value = response.data.message || '支付失败'
            return
        }

        await fetchOrderDetail()
    } catch (error) {
        errorMessage.value = resolveApiError(error, '支付失败，请稍后重试')
    } finally {
        paying.value = false
    }
}

function startCountdownTimer() {
    countdownTimer = window.setInterval(() => {
        now.value = Date.now()

        if (paymentRemainingSeconds.value === 0 && isPendingPayment.value && !timeoutRefreshTriggered.value) {
            timeoutRefreshTriggered.value = true
            void fetchOrderDetail()
        }
    }, 1000)
}

onMounted(() => {
    void fetchOrderDetail()
    startCountdownTimer()
})

onUnmounted(() => {
    if (countdownTimer !== undefined) {
        window.clearInterval(countdownTimer)
    }
})
</script>

<template>
    <div class="min-h-screen bg-muted/30">
        <Navbar />

        <main class="container mx-auto px-4 py-6">
            <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
                <Button variant="ghost" class="px-0" @click="router.push('/orders')">
                    <ArrowLeft class="mr-2 size-4" />
                    返回订单列表
                </Button>

                <div v-if="canPay || canCancel" class="flex items-center gap-2">
                    <Button v-if="canPay" :disabled="paying || canceling" @click="handlePayOrder">
                        <CreditCard class="mr-2 size-4" />
                        {{ paying ? '支付中...' : '立即支付' }}
                    </Button>

                    <Button v-if="canCancel" variant="outline" :disabled="canceling || paying" @click="handleCancelOrder">
                        <RotateCcw class="mr-2 size-4" />
                        {{ canceling ? '取消中...' : '取消订单' }}
                    </Button>
                </div>
            </div>

            <div v-if="errorMessage" class="mb-4 rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {{ errorMessage }}
            </div>

            <div v-if="loading" class="grid gap-4">
                <div class="h-28 animate-pulse rounded-lg bg-muted" />
                <div class="h-72 animate-pulse rounded-lg bg-muted" />
            </div>

            <div v-else-if="!order" class="rounded-lg border border-dashed bg-background px-6 py-16 text-center">
                <PackageX class="mx-auto size-10 text-muted-foreground" />
                <h1 class="mt-4 text-lg font-semibold">未找到订单</h1>
                <p class="mt-2 text-sm text-muted-foreground">请从订单列表重新进入。</p>
            </div>

            <div v-else class="grid gap-6 xl:grid-cols-[1fr_360px]">
                <section class="space-y-4">
                    <Card>
                        <CardHeader>
                            <div class="flex flex-wrap items-start justify-between gap-3">
                                <div>
                                    <CardTitle class="flex items-center gap-2">
                                        <ReceiptText class="size-5 text-orange-500" />
                                        订单 {{ order.orderNo }}
                                    </CardTitle>
                                    <CardDescription class="mt-2">创建时间：{{ order.createTime }}</CardDescription>
                                    <CardDescription
                                        v-if="paymentCountdownText"
                                        class="mt-2 flex items-center gap-1.5 text-orange-600"
                                    >
                                        <Clock class="size-3.5" />
                                        {{ paymentCountdownText }}
                                    </CardDescription>
                                </div>

                                <Badge variant="secondary">{{ resolveStatusText(order.status) }}</Badge>
                            </div>
                        </CardHeader>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>商品明细</CardTitle>
                            <CardDescription>商品名称、下单价格和数量以下单时的信息为准。</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-3">
                            <div
                                v-for="item in order.items"
                                :key="item.id"
                                class="grid gap-4 rounded-lg border bg-background p-4 sm:grid-cols-[88px_1fr_auto]"
                            >
                                <div class="flex size-22 items-center justify-center overflow-hidden rounded-md bg-muted">
                                    <img
                                        v-if="item.productImage"
                                        :src="item.productImage"
                                        :alt="item.productName"
                                        class="aspect-square w-[88px] object-cover"
                                    />
                                    <Truck v-else class="size-6 text-muted-foreground" />
                                </div>

                                <div>
                                    <h2 class="font-semibold leading-7">{{ item.productName }}</h2>
                                    <p class="mt-1 text-sm text-muted-foreground">
                                        单价 ¥{{ formatPrice(item.productPrice) }} × {{ item.quantity }}
                                    </p>
                                </div>

                                <div class="text-left sm:text-right">
                                    <p class="text-xs text-muted-foreground">小计</p>
                                    <p class="mt-1 text-lg font-bold">¥{{ formatPrice(item.subtotal) }}</p>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>收货信息</CardTitle>
                        </CardHeader>
                        <CardContent class="grid gap-2 text-sm text-muted-foreground">
                            <p><span class="text-foreground">收货人：</span>{{ order.receiverName }}</p>
                            <p><span class="text-foreground">手机号：</span>{{ order.receiverPhone }}</p>
                            <p><span class="text-foreground">收货地址：</span>{{ order.receiverAddress }}</p>
                        </CardContent>
                    </Card>
                </section>

                <aside>
                    <Card class="xl:sticky xl:top-24">
                        <CardHeader>
                            <CardTitle>金额汇总</CardTitle>
                        </CardHeader>
                        <CardContent class="space-y-4">
                            <div class="space-y-3 rounded-lg bg-muted/50 p-4 text-sm">
                                <div class="flex items-center justify-between">
                                    <span class="text-muted-foreground">商品金额</span>
                                    <span>¥{{ formatPrice(order.productAmount) }}</span>
                                </div>
                                <div class="flex items-center justify-between">
                                    <span class="text-muted-foreground">优惠金额</span>
                                    <span>-¥{{ formatPrice(order.discountAmount) }}</span>
                                </div>
                                <div class="flex items-center justify-between">
                                    <span class="text-muted-foreground">运费</span>
                                    <span>{{ order.shippingFee === 0 ? '免运费' : `¥${formatPrice(order.shippingFee)}` }}</span>
                                </div>
                                <div class="h-px bg-border" />
                                <div class="flex items-end justify-between">
                                    <span class="font-medium">应付金额</span>
                                    <span class="text-2xl font-bold text-orange-500">¥{{ formatPrice(order.payableAmount) }}</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </aside>
            </div>
        </main>
    </div>
</template>
