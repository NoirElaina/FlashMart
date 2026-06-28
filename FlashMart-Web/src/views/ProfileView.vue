<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Bell, CreditCard, LogOut, MapPin, Package, ShieldCheck, Sparkles } from 'lucide-vue-next'
import Navbar from '@/layout/Navbar.vue'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
    if (!userStore.isLoggedIn) {
        router.replace('/login')
    }
})

const displayName = computed(() => userStore.username || '未登录用户')
const userInitial = computed(() => displayName.value.trim().charAt(0).toUpperCase() || 'U')
const memberSince = computed(() => {
    const token = userStore.token
    if (!token) {
        return '欢迎加入 FlashMart'
    }

    try {
        const parts = token.split('.')
        if (parts.length < 2 || !parts[1]) {
            return '已登录 FlashMart'
        }

        const payloadPart = parts[1]
        const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
        const padded = normalized.padEnd(normalized.length + (4 - normalized.length % 4) % 4, '=')
        const payload = JSON.parse(atob(padded))
        if (!payload?.iat) {
            return '已登录 FlashMart'
        }

        return `加入于 ${new Date(payload.iat * 1000).toLocaleDateString('zh-CN')}`
    } catch {
        return '已登录 FlashMart'
    }
})

const summaryCards = [
    { title: '待收货', value: '3', icon: Package, accent: 'bg-orange-500/10 text-orange-600' },
    { title: '优惠提醒', value: '8', icon: Bell, accent: 'bg-blue-500/10 text-blue-600' },
    { title: '会员积分', value: '1260', icon: Sparkles, accent: 'bg-violet-500/10 text-violet-600' },
]

const quickActions = [
    { title: '收货地址', description: '管理默认地址与联系人', icon: MapPin },
    { title: '支付方式', description: '查看常用支付工具', icon: CreditCard },
    { title: '账号安全', description: '更新密码与登录设置', icon: ShieldCheck },
]

function handleLogout() {
    userStore.logout()
    router.push('/login')
}

function goHome() {
    router.push('/')
}
</script>

<template>
    <div class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#f2f6fc_100%)]">
        <Navbar />

        <main class="container mx-auto px-4 py-6">
            <section
                class="overflow-hidden rounded-3xl border border-white/70 bg-white/75 shadow-[0_24px_60px_rgba(130,153,192,.12)] backdrop-blur">
                <div
                    class="grid gap-8 bg-[radial-gradient(circle_at_top_right,rgba(130,170,255,.16),transparent_28%),linear-gradient(135deg,rgba(255,255,255,.96),rgba(244,248,255,.9))] p-6 md:grid-cols-[1.15fr_.85fr] md:p-8">
                    <div class="space-y-6">
                        <Button variant="ghost" class="w-fit px-0 text-muted-foreground" @click="goHome">
                            <ArrowLeft class="mr-2 size-4" />
                            返回首页
                        </Button>

                        <div class="flex items-start gap-4">
                            <Avatar class="size-[4.5rem] border border-white/80 shadow-lg">
                                <AvatarFallback class="bg-primary/10 text-xl font-bold text-primary">
                                    {{ userInitial }}
                                </AvatarFallback>
                            </Avatar>

                            <div class="space-y-3">
                                <div class="space-y-1">
                                    <p class="text-sm font-medium text-muted-foreground">个人中心</p>
                                    <h1 class="text-3xl font-bold tracking-tight text-slate-900 md:text-4xl">
                                        {{ displayName }}
                                    </h1>
                                    <p class="text-sm text-muted-foreground">
                                        {{ memberSince }}
                                    </p>
                                </div>

                                <div class="flex flex-wrap items-center gap-2">
                                    <Badge class="bg-blue-600 hover:bg-blue-600">活跃会员</Badge>
                                    <Badge variant="secondary">FlashMart 用户</Badge>
                                </div>
                            </div>
                        </div>

                        <p class="max-w-2xl text-sm leading-7 text-slate-600 md:text-base">
                            在这里统一查看你的账号信息、订单状态、优惠提醒和常用资料。后续我们也可以继续把订单记录、收货地址和安全设置接进来。
                        </p>

                        <div class="grid gap-4 sm:grid-cols-3">
                            <Card v-for="item in summaryCards" :key="item.title" class="border-white/80 bg-white/80">
                                <CardContent class="flex items-center justify-between p-5">
                                    <div class="space-y-1">
                                        <p class="text-sm text-muted-foreground">{{ item.title }}</p>
                                        <p class="text-2xl font-bold text-slate-900">{{ item.value }}</p>
                                    </div>
                                    <div class="flex size-11 items-center justify-center rounded-2xl" :class="item.accent">
                                        <component :is="item.icon" class="size-5" />
                                    </div>
                                </CardContent>
                            </Card>
                        </div>
                    </div>

                    <Card class="border-white/80 bg-white/85">
                        <CardHeader>
                            <CardTitle>账号概览</CardTitle>
                            <CardDescription>基础信息与快捷入口</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-5">
                            <div class="space-y-3">
                                <div class="rounded-2xl border bg-slate-50/80 p-4">
                                    <p class="text-xs font-medium uppercase tracking-wide text-muted-foreground">用户名</p>
                                    <p class="mt-2 text-sm font-semibold text-slate-900">{{ displayName }}</p>
                                </div>
                                <div class="rounded-2xl border bg-slate-50/80 p-4">
                                    <p class="text-xs font-medium uppercase tracking-wide text-muted-foreground">登录状态</p>
                                    <p class="mt-2 text-sm font-semibold text-slate-900">当前账号已登录并保持有效状态</p>
                                </div>
                            </div>

                            <div class="space-y-3">
                                <div
                                    v-for="item in quickActions"
                                    :key="item.title"
                                    class="flex items-center gap-3 rounded-2xl border bg-white p-4 transition-colors hover:bg-slate-50"
                                >
                                    <div class="flex size-10 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                                        <component :is="item.icon" class="size-5" />
                                    </div>
                                    <div class="min-w-0">
                                        <p class="text-sm font-semibold text-slate-900">{{ item.title }}</p>
                                        <p class="text-xs text-muted-foreground">{{ item.description }}</p>
                                    </div>
                                </div>
                            </div>

                            <Button variant="outline" class="w-full justify-center" @click="handleLogout">
                                <LogOut class="mr-2 size-4" />
                                退出登录
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </section>
        </main>
    </div>
</template>
