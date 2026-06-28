<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, MessageSquareText, PackageCheck, ShieldCheck, Star } from 'lucide-vue-next'
import Navbar from '@/layout/Navbar.vue'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useCartStore } from '@/stores/cartStore'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { getProductDetail } from '@/api/product'
import { addCartItem } from '@/api/cart'
import { resolveApiError } from '@/utils/apiError'
import type { ProductDetail } from '@/types/product'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref<ProductDetail | null>(null)
const activeImage = ref('')
const loading = ref(true)
const errorMessage = ref('')
const addingToCart = ref(false)
const addToCartMessage = ref('')

const productId = computed(() => Number(route.params.id))

const averageRating = computed(() => {
    const rating = product.value?.reviewSummary?.avgRating
    return rating ? rating.toFixed(1) : '0.0'
})

const reviewCount = computed(() => product.value?.reviewSummary?.totalCount ?? 0)

const latestReviews = computed(() => product.value?.reviewSummary?.latestReviews ?? [])



const productImages = computed(() => {
    if (!product.value) {
        return []
    }
    const images = product.value.images.filter(Boolean)
    if (images.length > 0) {
        return images
    }
    return product.value.mainImage ? [product.value.mainImage] : []
})

const specsEntries = computed(() => Object.entries(product.value?.specs ?? {}))
const isSeckillProduct = computed(() => product.value?.seckill !== null && product.value?.seckill !== undefined)

const stockDisplayText = computed(() => {
    return product.value?.seckill?.stock?.displayText || `已售 ${product.value?.sold ?? 0} 件`
})

const seckillStatusText = computed(() => product.value?.seckill?.statusText || '当前无秒杀活动')
const isSeckillActive = computed(() => product.value?.seckill?.isActive === true)
const primaryActionDisabled = computed(() => isSeckillProduct.value && !isSeckillActive.value)
const primaryActionText = computed(() => {
    if (!isSeckillProduct.value) {
        return '立即购买'
    }

    return isSeckillActive.value ? '立即抢购' : seckillStatusText.value
})

const heroDescription = computed(() => {
    if (!product.value) {
        return ''
    }
    return '查看商品详情、规格参数、活动信息和用户评价。'
})

function discount(original: number, sale: number) {
    if (!original || original <= 0) {
        return 0
    }
    return Math.round((sale / original) * 10)
}

function goBack() {
    router.back()
}

async function addToCart() {
    if (!product.value || addingToCart.value) {
        return
    }

    addingToCart.value = true
    addToCartMessage.value = ''

    try {
        const response = await addCartItem({
            productId: product.value.id,
            quantity: 1,
        })

        if (response.data.code !== 200) {
            addToCartMessage.value = response.data.message || '加入购物车失败'
            return
        }

        addToCartMessage.value = '已加入购物车'
        cartStore.increaseCount(1)
    } catch (error) {
        addToCartMessage.value = resolveApiError(error, '加入购物车失败，请稍后再试')
    } finally {
        addingToCart.value = false
    }
}

function buyNow() {
    if (!product.value) {
        return
    }

    if (primaryActionDisabled.value) {
        addToCartMessage.value = seckillStatusText.value
        return
    }

    // 立即购买和秒杀抢购都先进入结算页填写收货信息；秒杀提交时会走专用下单接口。
    router.push({
        path: '/checkout',
        query: {
            mode: isSeckillProduct.value ? 'SECKILL' : 'BUY_NOW',
            productId: String(product.value.id),
            quantity: '1',
        },
    })
}

async function fetchProductDetailPage() {
    if (!Number.isFinite(productId.value) || productId.value <= 0) {
        errorMessage.value = '商品编号无效'
        loading.value = false
        return
    }

    loading.value = true
    errorMessage.value = ''

    try {
        const response = await getProductDetail(productId.value)
        const detail = response.data.data

        if (response.data.code !== 200) {
            errorMessage.value = response.data.message || '商品详情加载失败'
            product.value = null
            return
        }

        if (!detail) {
            errorMessage.value = '商品详情暂无数据'
            product.value = null
            return
        }

        product.value = detail
        activeImage.value = detail.mainImage || detail.images[0] || ''
    } catch (error) {
        console.error('获取商品详情失败:', error)
        product.value = null
        activeImage.value = ''
        errorMessage.value = '商品详情加载失败，请稍后重试'
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchProductDetailPage()
})
</script>

<template>
    <div class="min-h-screen bg-[linear-gradient(180deg,#fff7f0_0%,#fff_20%,#f8fafc_100%)]">
        <Navbar />

        <main class="container mx-auto px-4 py-6">
            <div class="mb-6 flex items-center gap-3 text-sm text-muted-foreground">
                <Button variant="ghost" class="px-0" @click="goBack">
                    <ArrowLeft class="mr-2 size-4" />
                    返回
                </Button>
                <span>/</span>
                <span>商品详情</span>
            </div>

            <div v-if="loading" class="grid gap-6 lg:grid-cols-[0.84fr_1.16fr]">
                <Card class="overflow-hidden">
                    <div class="mx-auto aspect-square max-w-[440px] animate-pulse rounded-3xl bg-muted" />
                </Card>
                <Card>
                    <CardContent class="space-y-4 p-6">
                        <div class="h-6 w-2/3 animate-pulse rounded bg-muted" />
                        <div class="h-4 w-full animate-pulse rounded bg-muted" />
                        <div class="h-4 w-1/2 animate-pulse rounded bg-muted" />
                        <div class="h-16 animate-pulse rounded bg-muted" />
                    </CardContent>
                </Card>
            </div>

            <div v-else-if="errorMessage"
                class="rounded-2xl border border-red-200 bg-red-50 px-6 py-10 text-center text-red-600">
                {{ errorMessage }}
            </div>

            <div v-else-if="product" class="space-y-6">
                <section class="grid gap-6 lg:grid-cols-[0.84fr_1.16fr] lg:items-start">
                    <Card class="overflow-hidden border-white/70 bg-white/90 shadow-[0_20px_50px_rgba(15,23,42,.06)]">
                        <CardContent class="p-4 md:p-6">
                            <div class="mx-auto overflow-hidden rounded-3xl bg-slate-100 max-w-[440px]">
                                <img :src="activeImage" :alt="product.name"
                                    class="aspect-square max-h-[440px] w-full object-cover" />
                            </div>

                            <div class="mx-auto mt-4 grid max-w-[440px] grid-cols-4 gap-3">
                                <button v-for="(image, index) in productImages" :key="`${image}-${index}`" type="button"
                                    class="overflow-hidden rounded-2xl border-2 bg-slate-100 transition"
                                    :class="activeImage === image ? 'border-orange-500' : 'border-transparent'"
                                    @click="activeImage = image">
                                    <img :src="image" :alt="product.name" class="aspect-square w-full object-cover" />
                                </button>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-white/70 bg-white/92 shadow-[0_20px_50px_rgba(15,23,42,.06)]">
                        <CardContent class="space-y-5 p-6 md:p-7">
                            <div class="flex flex-wrap items-center gap-2">
                                <Badge v-if="product.isHot" class="bg-orange-500 hover:bg-orange-500">热卖</Badge>
                                <Badge variant="secondary">{{ product.category }}</Badge>
                                <Badge v-if="discount(product.originalPrice, product.salePrice) > 0"
                                    class="bg-red-600 hover:bg-red-600">
                                    {{ discount(product.originalPrice, product.salePrice) }} 折
                                </Badge>
                                <Badge variant="outline">{{ seckillStatusText }}</Badge>
                            </div>

                            <div class="space-y-3">
                                <h1 class="text-3xl font-black tracking-tight text-slate-900 md:text-4xl">
                                    {{ product.name }}
                                </h1>
                                <p class="text-sm leading-7 text-slate-600">
                                    {{ heroDescription }}
                                </p>
                            </div>

                            <div class="rounded-3xl bg-[linear-gradient(135deg,#fff1e8_0%,#fff8f4_100%)] p-5">
                                <div class="flex flex-wrap items-end gap-3">
                                    <span class="text-3xl font-black text-red-500 md:text-4xl">¥{{ product.salePrice
                                    }}</span>
                                    <span class="text-base text-muted-foreground line-through">¥{{ product.originalPrice
                                    }}</span>
                                </div>
                                <div class="mt-3 flex flex-wrap gap-5 text-sm text-slate-600">
                                    <span>{{ stockDisplayText }}</span>
                                    <span>已售 {{ product.sold }} 件</span>
                                    <span>{{ product.limitPerUser ? `每人限购 ${product.limitPerUser} 件` : '不限购' }}</span>
                                </div>
                            </div>

                            <div class="grid gap-3 sm:grid-cols-3">
                                <div class="rounded-2xl border bg-slate-50/80 p-4">
                                    <div class="flex items-center gap-2 text-sm font-semibold text-slate-900">
                                        <Star class="size-4 text-amber-500" />
                                        商品评分
                                    </div>
                                    <p class="mt-2 text-2xl font-bold">{{ averageRating }}</p>
                                </div>
                                <div class="rounded-2xl border bg-slate-50/80 p-4">
                                    <div class="flex items-center gap-2 text-sm font-semibold text-slate-900">
                                        <MessageSquareText class="size-4 text-sky-600" />
                                        评论数
                                    </div>
                                    <p class="mt-2 text-2xl font-bold">{{ reviewCount }}</p>
                                </div>
                                <div class="rounded-2xl border bg-slate-50/80 p-4">
                                    <div class="flex items-center gap-2 text-sm font-semibold text-slate-900">
                                        <PackageCheck class="size-4 text-emerald-600" />
                                        秒杀状态
                                    </div>
                                    <p class="mt-2 text-lg font-bold">{{ seckillStatusText }}</p>
                                </div>
                            </div>

                            <div class="flex flex-col gap-3 sm:flex-row">
                                <Button class="h-11 flex-1 bg-orange-500 text-base hover:bg-orange-600 disabled:cursor-not-allowed disabled:opacity-60" :disabled="primaryActionDisabled" @click="buyNow">{{
                                    primaryActionText }}</Button>
                                <Button v-if="!isSeckillProduct" variant="outline" class="h-11 flex-1 text-base"
                                    :disabled="addingToCart" @click="addToCart">
                                    {{ addingToCart ? '加入中...' : '加入购物车' }}
                                </Button>
                            </div>

                            <p v-if="addToCartMessage" class="text-sm text-slate-600">
                                {{ addToCartMessage }}
                            </p>

                            <div class="rounded-2xl border bg-slate-50/70 p-4 text-sm leading-7 text-slate-600">
                                <div class="flex items-center gap-2 font-semibold text-slate-900">
                                    <ShieldCheck class="size-4 text-emerald-600" />
                                    服务保障
                                </div>
                                <p class="mt-2">支持 7 天无理由退换、平台正品保障与售后协助，当前版本先展示前端结构。</p>
                            </div>
                        </CardContent>
                    </Card>
                </section>

                <Tabs default-value="detail" class="space-y-4">
                    <TabsList class="grid w-full max-w-[420px] grid-cols-3">
                        <TabsTrigger value="detail">图文详情</TabsTrigger>
                        <TabsTrigger value="specs">规格参数</TabsTrigger>
                        <TabsTrigger value="reviews">用户评论</TabsTrigger>
                    </TabsList>

                    <TabsContent value="detail">
                        <Card class="border-white/70 bg-white/92">
                            <CardHeader>
                                <CardTitle>商品详情</CardTitle>
                                <CardDescription>对应 `ProductDetailVO.description` 的展示区域</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div class="prose prose-slate max-w-none leading-8" v-html="product.description" />
                            </CardContent>
                        </Card>
                    </TabsContent>

                    <TabsContent value="specs">
                        <Card class="border-white/70 bg-white/92">
                            <CardHeader>
                                <CardTitle>规格参数</CardTitle>
                                <CardDescription>对应 `ProductDetailVO.specs` 的键值展示</CardDescription>
                            </CardHeader>
                            <CardContent class="grid gap-3 md:grid-cols-2">
                                <div v-for="[key, value] in specsEntries" :key="key"
                                    class="rounded-2xl border bg-slate-50/70 px-4 py-3">
                                    <p class="text-xs uppercase tracking-wide text-muted-foreground">{{ key }}</p>
                                    <p class="mt-2 text-sm font-semibold text-slate-900">{{ value }}</p>
                                </div>
                            </CardContent>
                        </Card>
                    </TabsContent>

                    <TabsContent value="reviews">
                        <Card class="border-white/70 bg-white/92">
                            <CardHeader>
                                <CardTitle>用户评论</CardTitle>
                            </CardHeader>
                            <CardContent class="space-y-4">
                                <div v-for="review in latestReviews" :key="review.id"
                                    class="rounded-3xl border bg-slate-50/70 p-5">
                                    <div class="flex flex-wrap items-center justify-between gap-3">
                                        <div>
                                            <p class="font-semibold text-slate-900">{{ review.nickname }}</p>
                                            <p class="text-xs text-muted-foreground">{{ review.createTime }}</p>
                                        </div>
                                        <div class="flex items-center gap-1 text-amber-500">
                                            <Star v-for="index in 5" :key="index" class="size-4"
                                                :fill="index <= review.rating ? 'currentColor' : 'none'" />
                                        </div>
                                    </div>
                                    <p class="mt-4 text-sm leading-7 text-slate-700">{{ review.content }}</p>
                                    <div v-if="(review.images?.length ?? 0) > 0"
                                        class="mt-4 grid grid-cols-3 gap-3 md:grid-cols-5">
                                        <img v-for="(image, index) in review.images ?? []"
                                            :key="`${review.id}-${index}`" :src="image"
                                            :alt="`${review.nickname} 评论图片 ${index + 1}`"
                                            class="aspect-square w-full rounded-2xl object-cover" />
                                    </div>
                                </div>
                                <div v-if="latestReviews.length === 0"
                                    class="rounded-2xl border border-dashed px-4 py-10 text-center text-sm text-muted-foreground">
                                    暂无评论数据
                                </div>
                            </CardContent>
                        </Card>
                    </TabsContent>
                </Tabs>
            </div>
        </main>
    </div>
</template>
