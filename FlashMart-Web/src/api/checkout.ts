import { request } from '@/api/client'
import type { ApiResponse } from '@/types/api'
import type { CheckoutPreview, CheckoutPreviewPayload } from '@/types/checkout'

export function previewCheckout(payload: CheckoutPreviewPayload) {
    return request.post<ApiResponse<CheckoutPreview>>('/api/checkout/preview', payload)
}
