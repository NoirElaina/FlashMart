import { request } from '@/api/client'
import type { ApiResponse } from '@/types/api'
import type { OrderCreatePayload, OrderCreateResult, OrderDetail } from '@/types/order'

export function createOrder(payload: OrderCreatePayload) {
    return request.post<ApiResponse<OrderCreateResult>>('/api/orders', payload)
}

export function createSeckillOrder(payload: OrderCreatePayload) {
    return request.post<ApiResponse<OrderCreateResult>>('/api/orders/seckill', payload)
}

export function getOrders() {
    return request.get<ApiResponse<OrderDetail[]>>('/api/orders')
}

export function getOrderDetail(orderId: number) {
    return request.get<ApiResponse<OrderDetail>>(`/api/orders/${orderId}`)
}

export function cancelOrder(orderId: number) {
    return request.put<ApiResponse<string>>(`/api/orders/${orderId}/cancel`)
}

export function payOrder(orderId: number) {
    return request.put<ApiResponse<string>>(`/api/orders/${orderId}/pay`)
}
