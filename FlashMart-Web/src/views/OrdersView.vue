<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowLeft, PackageSearch } from 'lucide-vue-next'
import Navbar from '@/layout/Navbar.vue'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

const router = useRouter()
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
                        订单模块还在建设中，后续会展示待支付、已取消、已完成等订单状态。
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    <div
                        class="flex flex-col items-center justify-center rounded-lg border border-dashed px-6 py-16 text-center">
                        <PackageSearch class="size-10 text-muted-foreground" />
                        <h2 class="mt-4 text-lg font-semibold">暂无订单数据</h2>
                        <p class="mt-2 max-w-md text-sm text-muted-foreground">
                            这里会展示你的订单记录，并支持查看详情、继续支付或取消待支付订单。
                        </p>
                        <Button class="mt-6" @click="router.push('/')">
                            去逛商品
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </main>
    </div>
</template>
