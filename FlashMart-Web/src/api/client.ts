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

                if (router.currentRoute.value.path !== '/login') {
                    await router.push({
                        path: '/login',
                        query: {
                            redirect: router.currentRoute.value.fullPath,
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
