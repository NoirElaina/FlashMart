<script setup lang="ts">
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import type { Product } from '@/types/product'

defineProps<{
    products: Product[]
    discount: (original: number, sale: number) => number
}>()

const emit = defineEmits<{
    open: [productId: number]
}>()
</script>

<template>
    <div v-if="products.length === 0" class="rounded-lg border border-dashed bg-background px-6 py-12 text-center text-sm text-muted-foreground">
        当前筛选条件下暂无普通商品
    </div>

    <div v-else class="grid grid-cols-2 gap-3 md:grid-cols-4 xl:grid-cols-5">
        <Card
            v-for="product in products"
            :key="product.id"
            class="overflow-hidden hover:shadow-md transition-shadow cursor-pointer"
            @click="emit('open', product.id)"
        >
            <div class="relative bg-muted aspect-[4/4.2] overflow-hidden">
                <img :src="product.image" :alt="product.name" class="h-full w-full object-cover" />
                <Badge v-if="product.hot" class="absolute top-2 left-2 bg-orange-500 hover:bg-orange-500">热卖</Badge>
                <Badge
                    v-if="discount(product.originalPrice, product.salePrice) > 0"
                    class="absolute top-2 right-2 bg-red-600 hover:bg-red-600"
                >
                    {{ discount(product.originalPrice, product.salePrice) }}折
                </Badge>
            </div>

            <CardContent class="p-2 space-y-0.5">
                <p class="text-xs font-medium line-clamp-2 leading-snug">{{ product.name }}</p>
                <div class="flex items-baseline gap-2">
                    <span class="text-red-500 font-bold text-sm">¥{{ product.salePrice }}</span>
                    <span class="text-muted-foreground text-xs line-through">¥{{ product.originalPrice }}</span>
                </div>
                <p class="text-xs text-muted-foreground">已售 {{ product.sold }} 件</p>
            </CardContent>

            <CardFooter class="p-2 pt-0">
                <Button class="h-7 w-full bg-orange-500 hover:bg-orange-600 text-xs" size="sm" @click.stop="emit('open', product.id)">
                    查看详情
                </Button>
            </CardFooter>
        </Card>
    </div>
</template>
