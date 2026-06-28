import { request } from '@/api/client'
import type { ApiResponse, PageResult } from '@/types/api'
import type { Product, ProductDetail } from '@/types/product'

export interface ProductQueryParams {
    page: number
    pageSize: number
    category?: string
}

export function getProducts(params: ProductQueryParams) {
    return request.get<ApiResponse<PageResult<Product>>>('/api/products', { params })
}

export function getProductDetail(productId: number) {
    return request.get<ApiResponse<ProductDetail | null>>(`/api/products/${productId}`)
}
