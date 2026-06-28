import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    const initialToken = localStorage.getItem('token') || ''
    const initialUsername = localStorage.getItem('username') || ''

    const token = ref(initialToken)
    const username = ref(initialUsername)

    const isLoggedIn = computed(() => token.value.trim().length > 0)

    function setUser(newToken: string, newUsername: string) {
        if (!newToken.trim()) {
            logout()
            return
        }

        token.value = newToken
        username.value = newUsername
        localStorage.setItem('token', newToken)
        localStorage.setItem('username', newUsername)
    }

    function logout() {
        token.value = ''
        username.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('username')
    }

    return { token, username, isLoggedIn, setUser, logout }
})
