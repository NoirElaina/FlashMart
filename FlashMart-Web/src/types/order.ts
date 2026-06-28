export interface OrderCreatePayload {
    mode: 'CART' | 'BUY_NOW' | 'SECKILL'
    cartIds?: number[]
    productId?: number
    quantity?: number
    receiverName: string
    receiverPhone: string
    receiverAddress: string
}

export interface OrderCreateResult {
    orderId: number
    orderNo: string
    status: string
    payableAmount: number
}

export interface OrderItem {
    id: number
    productId: number
    productName: string
    productImage: string | null
    productPrice: number
    quantity: number
    subtotal: number
}

export interface OrderDetail {
    orderId: number
    orderNo: string
    status: string
    productAmount: number
    shippingFee: number
    discountAmount: number
    payableAmount: number
    receiverName: string
    receiverPhone: string
    receiverAddress: string
    payExpireTime: string
    closeDeadlineTime: string
    createTime: string
    items: OrderItem[]
}
