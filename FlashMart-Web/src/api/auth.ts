import { request } from '@/api/client'
import type { ApiResponse } from '@/types/api'

export interface AuthResponseData {
    accessToken: string
    refreshToken: string
    username: string
    role: string
}

export interface LoginPayload {
    username: string
    password: string
}

export interface RegisterPayload {
    username: string
    email: string
    password: string
}

export function login(payload: LoginPayload) {
    return request.post<ApiResponse<AuthResponseData>>('/api/user/login', payload)
}

export function register(payload: RegisterPayload) {
    return request.post<ApiResponse<AuthResponseData>>('/api/user/register', payload)
}
