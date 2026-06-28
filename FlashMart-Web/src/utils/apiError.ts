import axios from 'axios'
import type { ApiResponse } from '@/types/api'

export function resolveApiError(error: unknown, fallbackMessage: string) {
    if (axios.isAxiosError<ApiResponse<null>>(error)) {
        return error.response?.data?.message || fallbackMessage
    }

    return fallbackMessage
}
