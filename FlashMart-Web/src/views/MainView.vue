<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Timer, Flame } from 'lucide-vue-next'
import RegularProductTab from '@/components/RegularProductTab.vue'
import SeckillProductTab from '@/components/SeckillProductTab.vue'
import Navbar from '@/layout/Navbar.vue'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import {
    Pagination,
    PaginationContent as PaginationList,
    PaginationEllipsis,
    PaginationItem as PaginationListItem,
    PaginationNext,
    PaginationPrevious as PaginationPrev,
} from '@/components/ui/pagination'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import type { Product } from '@/types/product'
import { getProducts } from '@/api/product'
import { useSearchStore } from '@/stores/searchStore'



const router = useRouter()
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const loading = ref(false)
const errorMessage = ref('')
const pageSizeOptions = [4, 8, 12, 16]
const searchStore = useSearchStore()


const categories = ['全部', '手机数码', '家用电器', '服饰鞋包', '美妆护肤']
const activeCategory = ref('全部')
const activeShelf = ref('regular')
const products = ref<Product[]>([])
const currentTime = ref(Date.now())
let timerId: number | null = null

const visibleProducts = computed(() => {
    const keyword = searchStore.keyword.trim().toLowerCase()
    if (!keyword) {
        return products.value
    }

    return products.value.filter((product) =>
        product.name.toLowerCase().includes(keyword)
        || product.category.toLowerCase().includes(keyword),
    )
})

const seckillProducts = computed(() => visibleProducts.value.filter((product) => product.seckill))
const regularProducts = computed(() => visibleProducts.value.filter((product) => !product.seckill))


function discount(original: number, sale: number) {
    if (!original || original <= 0) {
        return 0
    }
    return Math.round((sale / original) * 10)
}

function getSeckillState(product: Product) {
    if (!product.seckill || !product.seckillStartTime || !product.seckillEndTime) {
        return 'none'
    }

    const now = currentTime.value
    const startTime = new Date(product.seckillStartTime).getTime()
    const endTime = new Date(product.seckillEndTime).getTime()

    if (now < startTime) {
        return 'upcoming'
    }
    if (now > endTime) {
        return 'ended'
    }
    return 'active'
}

function getSeckillLabel(product: Product) {
    const state = getSeckillState(product)
    if (state === 'active') {
        return '秒杀中'
    }
    if (state === 'upcoming') {
        return '即将开始'
    }
    if (state === 'ended') {
        return '已结束'
    }
    return ''
}

function openProductDetail(productId: number) {
    router.push(`/products/${productId}`)
}

async function fetchProducts() {
    loading.value = true
    errorMessage.value = ''
    try {
        const response = await getProducts({
            page: currentPage.value,
            pageSize: pageSize.value,
            category: activeCategory.value == "全部" ? undefined : activeCategory.value
        })
        const pageData = response.data.data
        products.value = pageData?.records ?? []
        total.value = pageData?.total ?? 0
        currentPage.value = pageData?.page ?? 1
    } catch (error) {
        console.error('获取商品数据失败:', error)
        errorMessage.value = '商品加载失败，请稍后重试'
        products.value = []
        total.value = 0
    } finally {
        loading.value = false
    }
}



watch([activeCategory, pageSize], () => {
    if (currentPage.value === 1) {
        fetchProducts()
    } else {
        currentPage.value = 1
    }
})

watch(currentPage, fetchProducts)

const countdown = computed(() => {
    const now = new Date(currentTime.value)
    const endOfWeek = new Date(now)
    const day = now.getDay()
    const daysUntilSunday = day === 0 ? 7 : 7 - day

    endOfWeek.setDate(now.getDate() + daysUntilSunday)
    endOfWeek.setHours(23, 59, 59, 999)

    const diff = Math.max(0, endOfWeek.getTime() - currentTime.value)
    const totalHours = Math.floor(diff / (1000 * 60 * 60))

    return {
        hours: totalHours.toString().padStart(2, '0'),
        minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)).toString().padStart(2, '0'),
        seconds: Math.floor((diff % (1000 * 60)) / 1000).toString().padStart(2, '0'),
    }
})

function startCountdown() {
    currentTime.value = Date.now()
    timerId = window.setInterval(() => {
        currentTime.value = Date.now()
    }, 1000)
}

onMounted(() => {
    fetchProducts()
    startCountdown()
})

onBeforeUnmount(() => {
    if (timerId !== null) {
        window.clearInterval(timerId)
    }
})

</script>

<template>
    <div class="min-h-screen bg-muted/30">
        <Navbar />

        <main class="container mx-auto px-4 py-6 space-y-6">
            <!-- 秒杀倒计时横幅 -->
            <div
                class="rounded-xl bg-gradient-to-r from-orange-500 to-red-500 text-white p-5 flex items-center justify-between">
                <div class="flex items-center gap-3">
                    <Flame class="size-8" />
                    <div>
                        <p class="text-xl font-bold">限时秒杀</p>
                        <p class="text-sm opacity-80">超值好物，抢完即止</p>
                    </div>
                </div>
                <div class="flex items-center gap-2 text-sm">
                    <Timer class="size-4" />
                    <span>距结束：</span>
                    <div class="flex gap-1">
                        <span class="bg-white/20 rounded px-2 py-1 font-mono font-bold">{{ countdown.hours }}</span>
                        <span class="font-bold">:</span>
                        <span class="bg-white/20 rounded px-2 py-1 font-mono font-bold">{{ countdown.minutes }}</span>
                        <span class="font-bold">:</span>
                        <span class="bg-white/20 rounded px-2 py-1 font-mono font-bold">{{ countdown.seconds }}</span>
                    </div>
                </div>
            </div>

            <!-- 分类导航 -->
            <div class="flex gap-2 flex-wrap">
                <Button v-for="cat in categories" :key="cat" :variant="activeCategory === cat ? 'default' : 'outline'"
                    size="sm" @click="activeCategory = cat">
                    {{ cat }}
                </Button>
            </div>

            <!-- 商品列表 -->
            <div v-if="errorMessage" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                {{ errorMessage }}
            </div>

            <div v-else-if="loading" class="grid grid-cols-2 md:grid-cols-4 xl:grid-cols-5 gap-3">
                <Card v-for="item in pageSize" :key="item" class="overflow-hidden">
                    <div class="aspect-[4/4.2] bg-muted animate-pulse" />
                    <CardContent class="p-2 space-y-1.5">
                        <div class="h-3 rounded bg-muted animate-pulse" />
                        <div class="h-3 w-2/3 rounded bg-muted animate-pulse" />
                        <div class="h-3 w-1/2 rounded bg-muted animate-pulse" />
                    </CardContent>
                </Card>
            </div>

            <div v-else-if="products.length === 0"
                class="rounded-lg border border-dashed bg-background px-6 py-12 text-center text-sm text-muted-foreground">
                当前分类下暂无商品
            </div>

            <div v-else>
                <Tabs v-model="activeShelf" class="space-y-4">
                    <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                        <TabsList class="grid w-full max-w-[360px] grid-cols-2">
                            <TabsTrigger value="regular">普通商品</TabsTrigger>
                            <TabsTrigger value="seckill" class="gap-2">
                                <Flame class="size-4" />
                                秒杀商品
                            </TabsTrigger>
                        </TabsList>

                        <div class="flex flex-wrap items-center gap-2">
                            <Badge v-if="activeShelf === 'seckill'" class="bg-red-600 hover:bg-red-600">
                                {{ seckillProducts.length }} 件活动商品
                            </Badge>
                            <Badge v-else variant="secondary">
                                {{ regularProducts.length }} 件普通商品
                            </Badge>
                        </div>
                    </div>

                    <TabsContent value="regular" class="space-y-3">
                        <RegularProductTab :products="regularProducts" :discount="discount" @open="openProductDetail" />
                    </TabsContent>

                    <TabsContent value="seckill" class="space-y-3">
                        <SeckillProductTab :products="seckillProducts" :discount="discount"
                            :get-seckill-label="getSeckillLabel" @open="openProductDetail" />
                    </TabsContent>
                </Tabs>
            </div>

            <!-- 分页 -->
            <div v-if="total > 0" class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                <div class="flex items-center gap-3 text-sm text-muted-foreground">
                    <span>每页显示</span>
                    <Select :model-value="String(pageSize)" @update:model-value="pageSize = Number($event)">
                        <SelectTrigger class="w-[110px] bg-background">
                            <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem v-for="size in pageSizeOptions" :key="size" :value="String(size)">
                                {{ size }} 条
                            </SelectItem>
                        </SelectContent>
                    </Select>
                    <span>共 {{ total }} 条</span>
                </div>

                <Pagination v-model:page="currentPage" :total="total" :items-per-page="pageSize" :sibling-count="1"
                    show-edges>
                    <PaginationList v-slot="{ items }" class="flex items-center gap-1">
                        <PaginationPrev />
                        <template v-for="(item, index) in items"
                            :key="item.type === 'page' ? `page-${item.value}` : `${item.type}-${index}`">
                            <PaginationListItem v-if="item.type === 'page'" :value="item.value" as-child>
                                <Button class="size-9 p-0"
                                    :variant="item.value === currentPage ? 'default' : 'outline'">
                                    {{ item.value }}
                                </Button>
                            </PaginationListItem>
                            <PaginationEllipsis v-else :index="index" />
                        </template>
                        <PaginationNext />
                    </PaginationList>
                </Pagination>
            </div>
        </main>
    </div>
</template>

<style></style>
