<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Clock, PackageSearch, ReceiptText } from 'lucide-vue-next'
import Navbar from '@/layout/Navbar.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { getOrders } from '@/api/order'
import { resolveApiError } from '@/utils/apiError'
import { formatOrderCountdown, getOrderPaymentRemainingSeconds } from '@/utils/orderCountdown'
import type { OrderDetail } from '@/types/order'

const router = useRouter()

const orders = ref<OrderDetail[]>([])
const loading = ref(false)
const errorMessage = ref('')
const now = ref(Date.now())
let countdownTimer: number | undefined

const hasOrders = computed(() => orders.value.length > 0)

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

function resolveOrderCountdownText(order: OrderDetail) {
    if (order.status !== 'PENDING_PAYMENT') {
        return ''
    }

    const remainingSeconds = getOrderPaymentRemainingSeconds(order.payExpireTime, now.value)
    if (remainingSeconds === null) {
        return ''
    }

    if (remainingSeconds === 0) {
        return '订单已超时，状态更新中'
    }

    return `剩余 ${formatOrderCountdown(remainingSeconds)} 支付`
}

async function fetchOrders() {
    loading.value = true
    errorMessage.value = ''

    try {
        const response = await getOrders()
        if (response.data.code !== 200) {
            errorMessage.value = response.data.message || '订单列表加载失败'
            return
        }

        orders.value = response.data.data ?? []
    } catch (error) {
        errorMessage.value = resolveApiError(error, '订单列表加载失败，请稍后重试')
    } finally {
        loading.value = false
    }
}

function startCountdownTimer() {
    countdownTimer = window.setInterval(() => {
        now.value = Date.now()
    }, 1000)
}

onMounted(() => {
    void fetchOrders()
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
            <Button variant="ghost" class="mb-4 px-0" @click="router.push('/')">
                <ArrowLeft class="mr-2 size-4" />
                返回首页
            </Button>

            <Card>
                <CardHeader>
                    <CardTitle>我的订单</CardTitle>
                    <CardDescription>
                        查看你的订单记录，待支付订单会显示剩余支付时间。
                    </CardDescription>
                </CardHeader>

                <CardContent class="space-y-4">
                    <div v-if="errorMessage" class="rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                        {{ errorMessage }}
                    </div>

                    <div v-if="loading" class="space-y-3">
                        <div v-for="item in 3" :key="item" class="h-24 animate-pulse rounded-lg bg-muted" />
                    </div>

                    <div
                        v-else-if="!hasOrders"
                        class="flex flex-col items-center justify-center rounded-lg border border-dashed px-6 py-16 text-center"
                    >
                        <PackageSearch class="size-10 text-muted-foreground" />
                        <h2 class="mt-4 text-lg font-semibold">暂无订单数据</h2>
                        <p class="mt-2 max-w-md text-sm text-muted-foreground">
                            完成结算后，订单会出现在这里。
                        </p>
                        <Button class="mt-6" @click="router.push('/')">
                            去逛商品
                        </Button>
                    </div>

                    <div v-else class="space-y-3">
                        <button
                            v-for="order in orders"
                            :key="order.orderId"
                            type="button"
                            class="w-full rounded-lg border bg-background p-4 text-left transition hover:border-orange-300 hover:bg-orange-50/40"
                            @click="router.push(`/orders/${order.orderId}`)"
                        >
                            <div class="flex flex-wrap items-start justify-between gap-3">
                                <div>
                                    <div class="flex items-center gap-2">
                                        <ReceiptText class="size-4 text-orange-500" />
                                        <span class="font-semibold">订单 {{ order.orderNo }}</span>
                                    </div>
                                    <p class="mt-2 text-sm text-muted-foreground">{{ order.createTime }}</p>
                                    <p
                                        v-if="resolveOrderCountdownText(order)"
                                        class="mt-1 flex items-center gap-1.5 text-xs text-orange-600"
                                    >
                                        <Clock class="size-3.5" />
                                        {{ resolveOrderCountdownText(order) }}
                                    </p>
                                </div>

                                <Badge variant="secondary">{{ resolveStatusText(order.status) }}</Badge>
                            </div>

                            <div class="mt-4 flex flex-wrap items-center justify-between gap-3 text-sm">
                                <span class="text-muted-foreground">共 {{ order.items?.length ?? 0 }} 类商品</span>
                                <span class="text-lg font-bold text-orange-500">¥{{ formatPrice(order.payableAmount) }}</span>
                            </div>
                        </button>
                    </div>
                </CardContent>
            </Card>
        </main>
    </div>
</template>
