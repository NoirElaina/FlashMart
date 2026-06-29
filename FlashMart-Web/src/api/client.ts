import axios from 'axios'
import type { AxiosInstance } from 'axios'
import router from '@/router'

export const request = axios.create({
    baseURL: '',
    timeout: 10000,
})

function setupAuthInterceptors(instance: AxiosInstance) {
    instance.interceptors.request.use((config) => {
        const token = localStorage.getItem('token')

        if (token) {
            // 所有受保护接口统一从这里携带 JWT，页面层不再重复拼 Authorization。
            config.headers = config.headers ?? {}
            config.headers.Authorization = `Bearer ${token}`
        }

        return config
    })

    instance.interceptors.response.use(
        (response) => response,
        async (error) => {
            if (error.response?.status === 401) {
                localStorage.removeItem('token')
                localStorage.removeItem('username')

                // 仅当当前页面本身需要登录时才跳转；首页等公开页遇到 401 只清失效 token，不重定向。
                const currentRoute = router.currentRoute.value
                const requiresAuth = currentRoute.matched.some((r) => r.meta.requiresAuth)
                if (requiresAuth && currentRoute.path !== '/login') {
                    await router.push({
                        path: '/login',
                        query: {
                            redirect: currentRoute.fullPath,
                        },
                    })
                }
            }

            return Promise.reject(error)
        },
    )
}

setupAuthInterceptors(axios)
setupAuthInterceptors(request)
