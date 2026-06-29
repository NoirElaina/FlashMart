import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const refreshToken = ref(localStorage.getItem('refreshToken') || '')
    const username = ref(localStorage.getItem('username') || '')
    const role = ref(localStorage.getItem('role') || '')

    const isLoggedIn = computed(() => token.value.trim().length > 0)

    function setUser(accessToken: string, newRefreshToken: string, newUsername: string, newRole: string) {
        if (!accessToken || !accessToken.trim()) {
            logout()
            return
        }

        token.value = accessToken
        refreshToken.value = newRefreshToken || ''
        username.value = newUsername || ''
        role.value = newRole || ''

        localStorage.setItem('token', token.value)
        localStorage.setItem('refreshToken', refreshToken.value)
        localStorage.setItem('username', username.value)
        localStorage.setItem('role', role.value)
    }

    function logout() {
        token.value = ''
        refreshToken.value = ''
        username.value = ''
        role.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
    }

    return { token, refreshToken, username, role, isLoggedIn, setUser, logout }
})
