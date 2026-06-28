<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, Search, Zap, User } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { useCartStore } from '@/stores/cartStore'
import { useUserStore } from '@/stores/userStore'
import { ref } from 'vue'
import { useSearchStore } from '@/stores/searchStore'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const searchStore = useSearchStore()
const searchInput = ref(searchStore.keyword)


function handleLogin() {
    router.push('/login')
}

function handleProfile() {
    router.push('/profile')
}

function submitSearch() {
    searchStore.setKeyword(searchInput.value)
    router.push('/')
}

function handleLogout() {
    userStore.logout()
    cartStore.resetCount()
    router.push('/')
}

function getUserInitial(username: string) {
    return username.trim().charAt(0).toUpperCase() || 'U'
}

onMounted(() => {
    cartStore.fetchCartCount()
})
</script>

<template>
    <header
        class="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div class="container mx-auto flex h-16 items-center gap-4 px-4">
            <!-- Logo -->

            <button type="button" class="flex shrink-0 items-center gap-1.5 text-lg font-bold text-primary"
                @click="router.push('/')">
                <Zap class="size-5 text-orange-500" />
                <span>FlashMart</span>
            </button>

            <!-- Search -->
            <div class="flex flex-1 items-center gap-2 max-w-md">
                <div class="relative w-full">
                    <Search class="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
                    <Input v-model="searchInput" placeholder="搜索商品..." class="pl-9" @keyup.enter="submitSearch" />
                </div>
                <Button size="sm" @click="submitSearch">搜索</Button>
            </div>

            <div class="ml-auto flex items-center gap-2">
                <!-- Cart -->
                <Button variant="ghost" size="icon" class="relative" @click="router.push('/cart')">
                    <ShoppingCart class="size-5" />
                    <Badge v-if="cartStore.hasItems"
                        class="absolute -top-1 -right-1 size-5 flex items-center justify-center p-0 text-[10px]">
                        {{ cartStore.itemsCount > 99 ? '99+' : cartStore.itemsCount }}
                    </Badge>
                </Button>

                <!-- User -->
                <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                        <Button variant="ghost" size="icon">
                            <Avatar class="size-8">
                                <AvatarFallback>
                                    <span v-if="userStore.isLoggedIn" class="text-xs font-semibold">
                                        {{ getUserInitial(userStore.username) }}
                                    </span>
                                    <User v-else class="size-4" />
                                </AvatarFallback>
                            </Avatar>
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                        <template v-if="userStore.isLoggedIn">
                            <DropdownMenuItem disabled>
                                当前用户：{{ userStore.username }}
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem @click="handleProfile">个人中心</DropdownMenuItem>
                            <DropdownMenuItem @click="router.push('/orders')">我的订单</DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem class="text-destructive" @click="handleLogout">退出登录</DropdownMenuItem>
                        </template>
                        <template v-else>
                            <DropdownMenuItem @click="handleLogin">登录账号</DropdownMenuItem>
                        </template>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>
        </div>
    </header>
</template>
