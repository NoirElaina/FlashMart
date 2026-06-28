export interface CheckoutItem {
    cartId: number | null
    productId: number
    productName: string
    productImage: string
    salePrice: number
    quantity: number
    subtotal: number
    stock: number
    available: boolean
    unavailableReason: string | null
}

export interface CheckoutPreview {
    items: CheckoutItem[]
    totalQuantity: number
    productAmount: number
    shippingFee: number
    discountAmount: number
    payableAmount: number
    canSubmit: boolean
}

export type CheckoutMode = 'CART' | 'BUY_NOW' | 'SECKILL'

export interface CheckoutPreviewPayload {
    mode: CheckoutMode
    cartIds?: number[]
    productId?: number
    quantity?: number
}
