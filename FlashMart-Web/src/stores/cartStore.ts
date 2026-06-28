import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useUserStore } from './userStore'
import { getCartCount } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
    const itemsCount = ref(0)

    const hasItems = computed(() => itemsCount.value > 0)

    async function fetchCartCount() {
        const userStore = useUserStore()
        if (!userStore.isLoggedIn) {
            itemsCount.value = 0
            return
        }

        try {
            const response = await getCartCount()
            itemsCount.value = response.data.data ?? 0
        } catch (error) {
            console.error('获取购物车角标失败:', error)
            itemsCount.value = 0
        }
    }

    function increaseCount(quantity = 1) {
        itemsCount.value += Math.max(1, quantity)
    }

    function setCount(count: number) {
        itemsCount.value = Math.max(0, count)
    }

    function resetCount() {
        itemsCount.value = 0
    }

    return { itemsCount, hasItems, fetchCartCount, increaseCount, setCount, resetCount }
})
