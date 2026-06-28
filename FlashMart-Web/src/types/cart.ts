export interface CartApiItem {
    id: number
    productId: number
    name: string
    image: string
    category: string
    salePrice: number
    originalPrice: number
    quantity: number
    selected: number
    stock: number
    sold: number
    limitPerUser: number | null
}

export interface CartItem extends Omit<CartApiItem, 'selected'> {
    selected: boolean
    stockText: string
    tag: string
}

export interface CartItemAddPayload {
    productId: number
    quantity: number
}

export interface CartItemUpdatePayload {
    quantity?: number
    selected?: number
}

export interface CartSelectedBatchPayload {
    cartIds: number[]
    selected: number
}

export interface CartBatchDeletePayload {
    cartIds: number[]
}
