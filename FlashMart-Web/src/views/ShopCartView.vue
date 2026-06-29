<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
    ArrowLeft,
    BadgeCheck,
    CreditCard,
    Minus,
    Plus,
    ShoppingCart,
    Sparkles,
    Trash2,
    Truck,
} from 'lucide-vue-next'
import Navbar from '@/layout/Navbar.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import {
    clearInvalidCartItems,
    deleteCartItem,
    deleteCartItems,
    getCartItems,
    updateCartItem as updateCartItemApi,
    updateCartSelectedBatch,
} from '@/api/cart'
import { useCartStore } from '@/stores/cartStore'
import { useUserStore } from '@/stores/userStore'
import { resolveApiError } from '@/utils/apiError'
import type { CartItem } from '@/types/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const cartItems = ref<CartItem[]>([])
const loading = ref(false)
const errorMessage = ref('')
const updatingItemIds = ref<number[]>([])
const batchUpdating = ref(false)

const selectedCount = computed(() => cartItems.value.filter((item) => item.selected && item.available).length)
const totalQuantity = computed(() => cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
const hasInvalidItems = computed(() => cartItems.value.some((item) => !item.available))
const subtotal = computed(() =>
    cartItems.value
        .filter((item) => item.selected && item.available)
        .reduce((sum, item) => sum + item.salePrice * item.quantity, 0),
)
const originalTotal = computed(() =>
    cartItems.value
        .filter((item) => item.selected && item.available)
        .reduce((sum, item) => sum + item.originalPrice * item.quantity, 0),
)
const savings = computed(() => Math.max(0, originalTotal.value - subtotal.value))
const shippingFee = computed(() => (selectedCount.value > 0 ? 0 : 18))
const payable = computed(() => subtotal.value + shippingFee.value)
const allSelected = computed({
  get: () => {
    const available = cartItems.value.filter((item) => item.available)
    return available.length > 0 && available.every((item) => item.selected)
  },
  set: () => {},
})

const recommendationTags = [
    { label: '满 299 免运费', icon: Truck },
    { label: '支持分期支付', icon: CreditCard },
    { label: '正品保障', icon: BadgeCheck },
]

function formatPrice(price: number) {
    return price.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    })
}

function goHome() {
    router.push('/')
}

function openProductDetail(productId: number) {
    router.push(`/products/${productId}`)
}

function isItemUpdating(itemId: number) {
    return updatingItemIds.value.includes(itemId)
}

function startItemUpdating(itemId: number) {
    if (!updatingItemIds.value.includes(itemId)) {
        updatingItemIds.value = [...updatingItemIds.value, itemId]
    }
}

function finishItemUpdating(itemId: number) {
    updatingItemIds.value = updatingItemIds.value.filter((id) => id !== itemId)
}

async function updateCartItem(itemId: number, payload: { quantity?: number; selected?: number }) {
    startItemUpdating(itemId)
    errorMessage.value = ''
    try {
        await updateCartItemApi(itemId, payload)
        await fetchCart()
    } catch (error) {
        errorMessage.value = resolveApiError(error, '更新购物车失败，请稍后重试')
    } finally {
        finishItemUpdating(itemId)
    }
}

async function updateQuantity(itemId: number, delta: number) {
    const item = cartItems.value.find((cartItem) => cartItem.id === itemId)
    if (!item || isItemUpdating(itemId) || batchUpdating.value) {
        return
    }

    const nextQuantity = Math.max(1, item.quantity + delta)
    if (nextQuantity === item.quantity) {
        return
    }

    await updateCartItem(itemId, { quantity: nextQuantity })
}

async function updateSelected(itemId: number, selected: boolean) {
    if (isItemUpdating(itemId) || batchUpdating.value) {
        return
    }
    await updateCartItem(itemId, { selected: selected ? 1 : 0 })
}

async function updateAllSelected(selected: boolean) {
    if (batchUpdating.value || cartItems.value.length === 0) {
        return
    }

    const targetItems = cartItems.value.filter((item) => item.available && item.selected !== selected)
    if (targetItems.length === 0) {
        return
    }

    batchUpdating.value = true
    errorMessage.value = ''
    try {
        await updateCartSelectedBatch({
            cartIds: targetItems.map((item) => item.id),
            selected: selected ? 1 : 0,
        })
        await fetchCart()
    } catch (error) {
        errorMessage.value = resolveApiError(error, '更新购物车勾选状态失败，请稍后重试')
    } finally {
        batchUpdating.value = false
    }
}

function handleSelectedChange(itemId: number, event: Event) {
    const target = event.target as HTMLInputElement | null
    if (!target) {
        return
    }
    void updateSelected(itemId, target.checked)
}

function handleAllSelectedChange(event: Event) {
    const target = event.target as HTMLInputElement | null
    if (!target) {
        return
    }
    void updateAllSelected(target.checked)
}

async function removeItem(itemId: number) {
    if (isItemUpdating(itemId) || batchUpdating.value) {
        return
    }

    startItemUpdating(itemId)
    errorMessage.value = ''
    try {
        await deleteCartItem(itemId)
        await fetchCart()
    } catch (error) {
        errorMessage.value = resolveApiError(error, '删除购物车商品失败，请稍后重试')
    } finally {
        finishItemUpdating(itemId)
    }
}

async function clearSelected() {
    const selectedIds = cartItems.value.filter((item) => item.selected).map((item) => item.id)
    if (selectedIds.length === 0 || batchUpdating.value) {
        return
    }

    batchUpdating.value = true
    errorMessage.value = ''
    try {
        await deleteCartItems({ cartIds: selectedIds })
        await fetchCart()
    } catch (error) {
        errorMessage.value = resolveApiError(error, '删除已选商品失败，请稍后重试')
    } finally {
        batchUpdating.value = false
    }
}

async function clearInvalid() {
    if (batchUpdating.value) {
        return
    }
    batchUpdating.value = true
    errorMessage.value = ''
    try {
        await clearInvalidCartItems()
        await fetchCart()
    } catch (error) {
        errorMessage.value = resolveApiError(error, '清理失效商品失败，请稍后重试')
    } finally {
        batchUpdating.value = false
    }
}

function goCheckout() {
    const selectedIds = cartItems.value
        .filter((item) => item.selected && item.available)
        .map((item) => item.id)
    if (selectedIds.length === 0) {
        errorMessage.value = '请选择要结算的有效商品'
        return
    }

    // 结算只传选中的购物车项，后端会再次按当前用户校验归属和库存。
    router.push({
        path: '/checkout',
        query: {
            mode: 'CART',
            cartIds: selectedIds.join(','),
        },
    })
}

function continueShopping() {
    router.push('/')
}

function resolveStockText(item: Omit<CartItem, 'stockText' | 'tag'>) {
    if (item.stock <= 0) {
        return '当前库存不足，请稍后再试'
    }
    if (item.stock <= item.quantity) {
        return `仅剩 ${item.stock} 件库存，请尽快结算`
    }
    if (item.limitPerUser) {
        return `现货充足，每人限购 ${item.limitPerUser} 件`
    }
    return `现货充足，已售 ${item.sold} 件`
}

function resolveTag(item: Pick<CartItem, 'salePrice' | 'originalPrice' | 'stock'>) {
    if (item.stock <= 3) {
        return '库存紧张'
    }
    if (item.originalPrice > item.salePrice) {
        return '直降'
    }
    return '在售'
}

async function fetchCart() {
    if (!userStore.isLoggedIn) {
        router.push('/login')
        return
    }

    loading.value = true
    errorMessage.value = ''

    try {
        const response = await getCartItems()
        const cartList = response.data.data ?? []

        cartItems.value = cartList.map((item) => {
            const baseItem = {
                ...item,
                selected: Number(item.selected) === 1,
                stock: item.stock ?? 0,
                sold: item.sold ?? 0,
                limitPerUser: item.limitPerUser ?? null,
                available: item.available ?? true,
                invalidReason: item.invalidReason ?? null,
            }

            return {
                ...baseItem,
                stockText: resolveStockText(baseItem),
                tag: resolveTag(baseItem),
            }
        })
        cartStore.setCount(cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
    } catch (error) {
        console.error('获取购物车失败:', error)
        errorMessage.value = '购物车加载失败，请稍后重试'
        cartItems.value = []
        cartStore.resetCount()
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchCart()
})
</script>

<template>
    <div class="min-h-screen bg-[linear-gradient(180deg,#fff8f2_0%,#f8fbff_30%,#f3f6fb_100%)]">
        <Navbar />

        <main class="container mx-auto px-4 py-6">
            <section class="mb-6 rounded-3xl border border-white/70 bg-white/82 p-5 shadow-[0_20px_50px_rgba(114,134,176,.12)] backdrop-blur md:p-6">
                <div class="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
                    <div class="space-y-3">
                        <Button variant="ghost" class="w-fit px-0 text-muted-foreground" @click="goHome">
                            <ArrowLeft class="mr-2 size-4" />
                            返回首页
                        </Button>

                        <div class="space-y-2">
                            <div class="flex flex-wrap items-center gap-2">
                                <Badge class="bg-orange-500 hover:bg-orange-500">购物车</Badge>
                                <Badge variant="secondary">共 {{ totalQuantity }} 件商品</Badge>
                                <Badge variant="secondary">已选 {{ selectedCount }} 件</Badge>
                            </div>
                            <h1 class="text-2xl font-black tracking-tight text-slate-900 md:text-3xl">
                                购物车商品
                            </h1>
                            <p class="text-sm leading-7 text-slate-600">
                                先确认商品、数量和价格，再继续去结算。
                            </p>
                        </div>
                    </div>

                    <div class="grid gap-3 sm:grid-cols-3 xl:min-w-[540px]">
                        <Card class="border-white/80 bg-white/90">
                            <CardContent class="p-4">
                                <p class="text-xs text-muted-foreground">已选商品</p>
                                <p class="mt-2 text-2xl font-black text-slate-900">{{ selectedCount }}</p>
                            </CardContent>
                        </Card>
                        <Card class="border-white/80 bg-white/90">
                            <CardContent class="p-4">
                                <p class="text-xs text-muted-foreground">已省金额</p>
                                <p class="mt-2 text-2xl font-black text-emerald-600">¥{{ formatPrice(savings) }}</p>
                            </CardContent>
                        </Card>
                        <Card class="border-white/80 bg-white/90">
                            <CardContent class="p-4">
                                <p class="text-xs text-muted-foreground">待支付</p>
                                <p class="mt-2 text-2xl font-black text-orange-500">¥{{ formatPrice(payable) }}</p>
                            </CardContent>
                        </Card>
                    </div>
                </div>

                <div class="mt-4 flex flex-wrap items-center gap-3 rounded-2xl bg-[linear-gradient(135deg,#fff4ea_0%,#fffaf6_100%)] px-4 py-3 text-sm text-slate-600">
                    <div class="flex items-center gap-2 font-medium text-slate-900">
                        <Sparkles class="size-4 text-orange-500" />
                        购物车优惠已为你整理
                    </div>
                    <div
                        v-for="item in recommendationTags"
                        :key="item.label"
                        class="flex items-center gap-2 rounded-full bg-white/90 px-3 py-1.5"
                    >
                        <component :is="item.icon" class="size-4 text-primary" />
                        <span>{{ item.label }}</span>
                    </div>
                </div>
            </section>

            <section class="grid gap-6 xl:grid-cols-[1.28fr_.72fr]">
                <div class="space-y-4">
                    <div v-if="errorMessage" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                        {{ errorMessage }}
                    </div>

                    <div v-if="loading" class="grid gap-4">
                        <Card v-for="item in 2" :key="item" class="overflow-hidden border-white/80 bg-white/92">
                            <CardContent class="p-5">
                                <div class="grid gap-5 lg:grid-cols-[auto_132px_1fr_auto] lg:items-center">
                                    <div class="size-4 rounded bg-muted animate-pulse" />
                                    <div class="aspect-square w-[132px] rounded-3xl bg-muted animate-pulse" />
                                    <div class="space-y-3">
                                        <div class="h-4 w-24 rounded bg-muted animate-pulse" />
                                        <div class="h-8 w-3/4 rounded bg-muted animate-pulse" />
                                        <div class="h-4 w-full rounded bg-muted animate-pulse" />
                                        <div class="h-6 w-40 rounded bg-muted animate-pulse" />
                                    </div>
                                    <div class="space-y-3">
                                        <div class="h-10 w-28 rounded-full bg-muted animate-pulse" />
                                        <div class="h-6 w-24 rounded bg-muted animate-pulse" />
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    </div>

                    <Card class="border-white/80 bg-white/88 shadow-[0_18px_40px_rgba(15,23,42,.05)]">
                        <CardContent class="flex flex-col gap-4 p-5 md:flex-row md:items-center md:justify-between">
                            <label class="flex items-center gap-3 text-sm font-medium text-slate-700">
                                <input
                                    :checked="allSelected"
                                    type="checkbox"
                                    class="size-4 rounded border-slate-300 text-orange-500 focus:ring-orange-500"
                                    :disabled="batchUpdating || loading"
                                    @change="handleAllSelectedChange"
                                />
                                全选商品
                            </label>

                            <div class="flex flex-wrap items-center gap-3">
                                <Badge variant="secondary">购物车共 {{ cartItems.length }} 条记录</Badge>
                                <Button v-if="hasInvalidItems" variant="outline" size="sm" :disabled="batchUpdating || loading" @click="clearInvalid">清理失效商品</Button>
                                <Button variant="outline" size="sm" @click="clearSelected">删除已选</Button>
                            </div>
                        </CardContent>
                    </Card>

                    <Card
                        v-for="item in cartItems"
                        :key="item.id"
                        class="overflow-hidden border-white/80 bg-white/92 shadow-[0_18px_40px_rgba(15,23,42,.05)]"
                        :class="{ 'opacity-60 grayscale': !item.available }"
                    >
                        <CardContent class="p-5">
                            <div class="grid gap-5 lg:grid-cols-[auto_132px_1fr_auto] lg:items-center">
                                <label class="flex items-center justify-center self-start pt-1 lg:self-center lg:pt-0">
                                    <input
                                        :checked="item.selected"
                                        type="checkbox"
                                        class="size-4 rounded border-slate-300 text-orange-500 focus:ring-orange-500"
                                        :disabled="!item.available || isItemUpdating(item.id) || batchUpdating || loading"
                                        @change="handleSelectedChange(item.id, $event)"
                                    />
                                </label>

                                <button
                                    type="button"
                                    class="overflow-hidden rounded-3xl bg-slate-100"
                                    @click="openProductDetail(item.productId)"
                                >
                                    <img :src="item.image" :alt="item.name" class="aspect-square w-[132px] object-cover" />
                                </button>

                                <div class="space-y-3">
                                    <div class="flex flex-wrap items-center gap-2">
                                        <Badge variant="secondary">{{ item.category }}</Badge>
                                        <Badge v-if="item.available && item.tag" class="bg-orange-500 hover:bg-orange-500">{{ item.tag }}</Badge>
                                        <Badge v-if="!item.available" class="bg-slate-500 hover:bg-slate-500">{{ item.invalidReason || '已失效' }}</Badge>
                                    </div>

                                    <button
                                        type="button"
                                        class="text-left text-lg font-bold leading-8 text-slate-900 transition-colors hover:text-orange-500"
                                        @click="openProductDetail(item.productId)"
                                    >
                                        {{ item.name }}
                                    </button>

                                    <p class="text-sm leading-7" :class="item.available ? 'text-slate-600' : 'text-red-500'">
                                        {{ item.available ? item.stockText : (item.invalidReason || '商品已失效，建议清理') }}
                                    </p>

                                    <div class="flex flex-wrap items-end gap-3">
                                        <span class="text-2xl font-black text-orange-500">¥{{ formatPrice(item.salePrice) }}</span>
                                        <span class="text-sm text-muted-foreground line-through">
                                            ¥{{ formatPrice(item.originalPrice) }}
                                        </span>
                                    </div>
                                </div>

                                <div class="flex flex-col items-start gap-4 lg:items-end">
                                    <div class="flex items-center rounded-full border bg-slate-50 p-1">
                                        <Button variant="ghost" size="icon" class="size-8 rounded-full" :disabled="!item.available || isItemUpdating(item.id) || batchUpdating || loading" @click="updateQuantity(item.id, -1)">
                                            <Minus class="size-4" />
                                        </Button>
                                        <span class="min-w-10 text-center text-sm font-semibold text-slate-900">
                                            {{ item.quantity }}
                                        </span>
                                        <Button variant="ghost" size="icon" class="size-8 rounded-full" :disabled="!item.available || isItemUpdating(item.id) || batchUpdating || loading" @click="updateQuantity(item.id, 1)">
                                            <Plus class="size-4" />
                                        </Button>
                                    </div>

                                    <div class="text-left lg:text-right">
                                        <p class="text-xs uppercase tracking-wide text-muted-foreground">小计</p>
                                        <p class="mt-1 text-lg font-bold text-slate-900">
                                            ¥{{ formatPrice(item.salePrice * item.quantity) }}
                                        </p>
                                    </div>

                                    <Button variant="ghost" size="sm" class="px-0 text-muted-foreground" @click="removeItem(item.id)">
                                        <Trash2 class="mr-2 size-4" />
                                        移除
                                    </Button>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card v-if="!loading && cartItems.length === 0" class="border-dashed border-white/80 bg-white/88">
                        <CardContent class="flex flex-col items-center px-6 py-14 text-center">
                            <div class="flex size-16 items-center justify-center rounded-full bg-slate-100 text-slate-500">
                                <ShoppingCart class="size-7" />
                            </div>
                            <h2 class="mt-5 text-2xl font-bold text-slate-900">购物车还是空的</h2>
                            <p class="mt-2 max-w-md text-sm leading-7 text-muted-foreground">
                                可以从首页或商品详情页继续挑选商品，加入购物车后会在这里展示。
                            </p>
                            <Button class="mt-6 bg-orange-500 hover:bg-orange-600" @click="goHome">去逛逛</Button>
                        </CardContent>
                    </Card>
                </div>

                <div class="space-y-4">
                    <Card class="border-white/80 bg-white/92 shadow-[0_18px_40px_rgba(15,23,42,.05)] xl:sticky xl:top-24">
                        <CardHeader>
                            <CardTitle>结算摘要</CardTitle>
                            <CardDescription>已选商品金额会在这里汇总</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-5">
                            <div class="space-y-3 rounded-3xl bg-slate-50/80 p-5">
                                <div class="flex items-center justify-between text-sm text-slate-600">
                                    <span>已选商品</span>
                                    <span>{{ selectedCount }} 件</span>
                                </div>
                                <div class="flex items-center justify-between text-sm text-slate-600">
                                    <span>商品总额</span>
                                    <span>¥{{ formatPrice(subtotal) }}</span>
                                </div>
                                <div class="flex items-center justify-between text-sm text-slate-600">
                                    <span>优惠立减</span>
                                    <span class="text-emerald-600">-¥{{ formatPrice(savings) }}</span>
                                </div>
                                <div class="flex items-center justify-between text-sm text-slate-600">
                                    <span>运费</span>
                                    <span>{{ shippingFee === 0 ? '免运费' : `¥${formatPrice(shippingFee)}` }}</span>
                                </div>
                                <div class="h-px bg-border" />
                                <div class="flex items-end justify-between">
                                    <span class="text-sm font-medium text-slate-700">应付金额</span>
                                    <span class="text-3xl font-black text-orange-500">¥{{ formatPrice(payable) }}</span>
                                </div>
                            </div>

                            <Button
                                class="h-12 w-full bg-orange-500 text-base font-bold hover:bg-orange-600"
                                :disabled="selectedCount === 0 || loading || batchUpdating"
                                @click="goCheckout"
                            >
                                去结算
                            </Button>
                            <Button variant="outline" class="h-11 w-full" @click="continueShopping">继续挑选商品</Button>

                            <div class="rounded-2xl border bg-[linear-gradient(135deg,#f8fbff_0%,#f3f7ff_100%)] p-4 text-sm leading-7 text-slate-600">
                                结算模块后续可以继续拆成优惠券、收货地址、配送方式和支付方式四块，这样自然就能衔接订单页。
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>
        </main>
    </div>
</template>
