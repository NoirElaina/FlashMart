<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AlertTriangle, ArrowLeft, PackageCheck, ShieldCheck, Truck } from 'lucide-vue-next'
import Navbar from '@/layout/Navbar.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { previewCheckout } from '@/api/checkout'
import { createOrder, createSeckillOrder } from '@/api/order'
import { resolveApiError } from '@/utils/apiError'
import type { CheckoutMode, CheckoutPreview, CheckoutPreviewPayload } from '@/types/checkout'

const route = useRoute()
const router = useRouter()

const preview = ref<CheckoutPreview | null>(null)
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const receiverName = ref('')
const receiverPhone = ref('')
const receiverAddress = ref('')
const receiverAddressMaxLength = 255

const mode = computed<CheckoutMode | null>(() => {
    if (route.query.mode === 'CART' || route.query.mode === 'BUY_NOW' || route.query.mode === 'SECKILL') {
        return route.query.mode
    }
    return null
})

const cartIds = computed(() =>
    String(route.query.cartIds || '')
        .split(',')
        .map((item) => Number(item))
        .filter((item) => Number.isInteger(item) && item > 0),
)

const productId = computed(() => Number(route.query.productId))

const quantity = computed(() => {
    const value = Number(route.query.quantity || 1)
    return Number.isInteger(value) && value > 0 ? value : 1
})

const checkoutPayload = computed<CheckoutPreviewPayload | null>(() => {
    if (mode.value === 'BUY_NOW' || mode.value === 'SECKILL') {
        return {
            mode: mode.value,
            productId: productId.value,
            quantity: quantity.value,
        }
    }

    if (mode.value === 'CART') {
        return {
            mode: 'CART',
            cartIds: cartIds.value,
        }
    }

    return null
})

const checkoutItems = computed(() => preview.value?.items ?? [])
const canSubmitOrder = computed(() => Boolean(preview.value?.canSubmit) && !loading.value && !submitting.value)
const modeLabel = computed(() => {
    if (mode.value === 'BUY_NOW') {
        return '立即购买'
    }
    if (mode.value === 'SECKILL') {
        return '秒杀抢购'
    }
    if (mode.value === 'CART') {
        return '购物车结算'
    }
    return '结算来源异常'
})
const sourceHint = computed(() => {
    if (mode.value === 'BUY_NOW') {
        return '当前订单来自商品详情页立即购买，不会写入购物车。'
    }
    if (mode.value === 'SECKILL') {
        return '当前订单来自秒杀活动，提交时会进行活动时间、库存和重复请求校验。'
    }
    if (mode.value === 'CART') {
        return '当前订单来自购物车选中项，提交后会同步更新购物车。'
    }
    return '请从商品详情页或购物车进入结算页。'
})

function hasValidCheckoutSource() {
    if (mode.value === 'BUY_NOW' || mode.value === 'SECKILL') {
        return Number.isInteger(productId.value) && productId.value > 0
    }
    if (mode.value === 'CART') {
        return cartIds.value.length > 0
    }

    return false
}

function formatPrice(price: number) {
    return price.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    })
}

async function fetchPreview() {
    if (!mode.value) {
        errorMessage.value = '结算来源不合法'
        return
    }
    if (!hasValidCheckoutSource()) {
        errorMessage.value = '请选择要结算的商品'
        return
    }

    loading.value = true
    errorMessage.value = ''
    successMessage.value = ''

    try {
        const payload = checkoutPayload.value
        if (!payload) {
            errorMessage.value = '结算来源不合法'
            return
        }

        const response = await previewCheckout(payload)
        if (response.data.code !== 200) {
            errorMessage.value = response.data.message || '结算信息加载失败'
            return
        }

        preview.value = response.data.data
    } catch (error) {
        errorMessage.value = resolveApiError(error, '结算信息加载失败')
    } finally {
        loading.value = false
    }
}

async function submitOrder() {
    if (!preview.value?.canSubmit) {
        errorMessage.value = '当前订单不可提交，请检查商品库存'
        return
    }
    if (!receiverName.value.trim() || !receiverPhone.value.trim() || !receiverAddress.value.trim()) {
        errorMessage.value = '请填写完整收货信息'
        return
    }

    submitting.value = true
    errorMessage.value = ''
    successMessage.value = ''

    try {
        const payload = checkoutPayload.value
        if (!payload) {
            errorMessage.value = '结算来源不合法'
            return
        }

        const orderPayload = {
            ...payload,
            receiverName: receiverName.value.trim(),
            receiverPhone: receiverPhone.value.trim(),
            receiverAddress: receiverAddress.value.trim(),
        }
        const response = mode.value === 'SECKILL'
            ? await createSeckillOrder(orderPayload)
            : await createOrder(orderPayload)

        if (response.data.code !== 200) {
            errorMessage.value = response.data.message || '提交订单失败'
            return
        }

        successMessage.value = `订单 ${response.data.data.orderNo} 已创建`
        await router.push(`/orders/${response.data.data.orderId}`)
    } catch (error) {
        errorMessage.value = resolveApiError(error, '提交订单失败，请稍后重试')
    } finally {
        submitting.value = false
    }
}

onMounted(fetchPreview)
</script>

<template>
    <div class="min-h-screen bg-muted/30">
        <Navbar />

        <main class="container mx-auto px-4 py-6">
            <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
                <Button variant="ghost" class="px-0" @click="router.back()">
                    <ArrowLeft class="mr-2 size-4" />
                    返回
                </Button>

                <Badge variant="secondary">{{ modeLabel }}</Badge>
            </div>

            <div class="grid gap-6 xl:grid-cols-[1fr_380px]">
                <section class="space-y-4">
                    <div v-if="errorMessage" class="flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                        <AlertTriangle class="mt-0.5 size-4 shrink-0" />
                        <span>{{ errorMessage }}</span>
                    </div>

                    <div v-if="successMessage" class="flex items-start gap-3 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                        <PackageCheck class="mt-0.5 size-4 shrink-0" />
                        <span>{{ successMessage }}</span>
                    </div>

                    <Card>
                        <CardHeader>
                            <CardTitle>确认订单</CardTitle>
                            <CardDescription>{{ sourceHint }}</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-4">
                            <div v-if="loading" class="space-y-3">
                                <div v-for="item in 2" :key="item" class="h-24 animate-pulse rounded-lg bg-muted" />
                            </div>

                            <div v-else-if="checkoutItems.length === 0" class="rounded-lg border border-dashed px-4 py-12 text-center text-sm text-muted-foreground">
                                暂无可结算商品
                            </div>

                            <div v-else class="space-y-3">
                                <div
                                    v-for="item in checkoutItems"
                                    :key="`${item.productId}-${item.cartId ?? 'buy-now'}`"
                                    class="grid gap-4 rounded-lg border bg-background p-4 sm:grid-cols-[96px_1fr_auto]"
                                >
                                    <img :src="item.productImage" :alt="item.productName" class="aspect-square w-24 rounded-md object-cover" />

                                    <div class="space-y-2">
                                        <div class="flex flex-wrap items-center gap-2">
                                            <h2 class="font-semibold leading-7 text-foreground">{{ item.productName }}</h2>
                                            <Badge v-if="!item.available" variant="destructive">不可提交</Badge>
                                        </div>
                                        <p class="text-sm text-muted-foreground">
                                            单价 ¥{{ formatPrice(item.salePrice) }} × {{ item.quantity }}
                                        </p>
                                        <p v-if="item.unavailableReason" class="text-sm text-destructive">
                                            {{ item.unavailableReason }}
                                        </p>
                                    </div>

                                    <div class="text-left sm:text-right">
                                        <p class="text-xs text-muted-foreground">小计</p>
                                        <p class="mt-1 text-lg font-bold">¥{{ formatPrice(item.subtotal) }}</p>
                                    </div>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle>收货信息</CardTitle>
                            <CardDescription>当前先使用页面填写，后续可升级为地址簿。</CardDescription>
                        </CardHeader>
                        <CardContent class="grid gap-4">
                            <div class="grid gap-2">
                                <Label for="receiverName">收货人</Label>
                                <Input id="receiverName" v-model="receiverName" maxlength="64" placeholder="请输入收货人姓名" />
                            </div>

                            <div class="grid gap-2">
                                <Label for="receiverPhone">手机号</Label>
                                <Input id="receiverPhone" v-model="receiverPhone" maxlength="32" placeholder="请输入手机号" />
                            </div>

                            <div class="grid gap-2">
                                <div class="flex items-center justify-between gap-3">
                                    <Label for="receiverAddress">收货地址</Label>
                                    <span class="text-xs text-muted-foreground">
                                        {{ receiverAddress.length }}/{{ receiverAddressMaxLength }}
                                    </span>
                                </div>
                                <Textarea
                                    id="receiverAddress"
                                    v-model="receiverAddress"
                                    :maxlength="receiverAddressMaxLength"
                                    placeholder="请输入详细收货地址"
                                />
                            </div>
                        </CardContent>
                    </Card>
                </section>

                <aside class="space-y-4">
                    <Card class="xl:sticky xl:top-24">
                        <CardHeader>
                            <CardTitle>结算摘要</CardTitle>
                            <CardDescription>请确认商品、优惠和应付金额。</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-5">
                            <div class="space-y-3 rounded-lg bg-muted/50 p-4 text-sm">
                                <div class="flex items-center justify-between">
                                    <span class="text-muted-foreground">商品数量</span>
                                    <span>{{ preview?.totalQuantity ?? 0 }} 件</span>
                                </div>
                                <div class="flex items-center justify-between">
                                    <span class="text-muted-foreground">商品金额</span>
                                    <span>¥{{ formatPrice(preview?.productAmount ?? 0) }}</span>
                                </div>
                                <div class="flex items-center justify-between">
                                    <span class="text-muted-foreground">优惠金额</span>
                                    <span>-¥{{ formatPrice(preview?.discountAmount ?? 0) }}</span>
                                </div>
                                <div class="flex items-center justify-between">
                                    <span class="text-muted-foreground">运费</span>
                                    <span>{{ (preview?.shippingFee ?? 0) === 0 ? '免运费' : `¥${formatPrice(preview?.shippingFee ?? 0)}` }}</span>
                                </div>
                                <div class="h-px bg-border" />
                                <div class="flex items-end justify-between">
                                    <span class="font-medium">应付金额</span>
                                    <span class="text-2xl font-bold text-orange-500">¥{{ formatPrice(preview?.payableAmount ?? 0) }}</span>
                                </div>
                            </div>

                            <Button class="h-11 w-full bg-orange-500 font-semibold hover:bg-orange-600" :disabled="!canSubmitOrder" @click="submitOrder">
                                {{ submitting ? '提交中...' : '提交订单' }}
                            </Button>

                            <div class="grid gap-2 rounded-lg border bg-background p-4 text-sm text-muted-foreground">
                                <div class="flex items-center gap-2">
                                    <ShieldCheck class="size-4 text-emerald-600" />
                                    <span>库存和价格会在提交前再次校验。</span>
                                </div>
                                <div class="flex items-center gap-2">
                                    <Truck class="size-4 text-sky-600" />
                                    <span>运费、优惠和可提交状态会实时更新。</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </aside>
            </div>
        </main>
    </div>
</template>
