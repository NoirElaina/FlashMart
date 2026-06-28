import { request } from '@/api/client'
import type { ApiResponse } from '@/types/api'
import type {
    CartApiItem,
    CartBatchDeletePayload,
    CartItemAddPayload,
    CartItemUpdatePayload,
    CartSelectedBatchPayload,
} from '@/types/cart'

export function getCartItems() {
    return request.get<ApiResponse<CartApiItem[]>>('/api/cart')
}

export function getCartCount() {
    return request.get<ApiResponse<number>>('/api/cart/count')
}

export function addCartItem(payload: CartItemAddPayload) {
    return request.post<ApiResponse<string>>('/api/cart', payload)
}

export function updateCartItem(cartId: number, payload: CartItemUpdatePayload) {
    return request.put<ApiResponse<string>>(`/api/cart/${cartId}`, payload)
}

export function deleteCartItem(cartId: number) {
    return request.delete<ApiResponse<string>>(`/api/cart/${cartId}`)
}

export function updateCartSelectedBatch(payload: CartSelectedBatchPayload) {
    return request.put<ApiResponse<string>>('/api/cart/selected/batch', payload)
}

export function deleteCartItems(payload: CartBatchDeletePayload) {
    return request.delete<ApiResponse<string>>('/api/cart/batch', {
        data: payload,
    })
}
