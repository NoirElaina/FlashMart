export interface Product {
    id: number
    name: string
    image: string
    category: string
    originalPrice: number
    salePrice: number
    stock: number
    sold: number
    hot: boolean
    seckill: boolean
    limitPerUser: number | null
    status: number
    seckillStartTime: string | null
    seckillEndTime: string | null
    createTime: string | null
    updateTime: string | null
}

export interface StockInfo {
    displayText: string
    available: boolean
}

export interface SeckillInfo {
    startTime: string | null
    endTime: string | null
    countdownSeconds: number | null
    isActive: boolean
    statusText: string
    stock: StockInfo | null
}

export interface Review {
    id: number
    rating: number
    content: string
    images: string[]
    createTime: string
    nickname: string
    avatar: string | null
}

export interface ReviewSummary {
    avgRating: number | null
    totalCount: number
    distribution: Record<number, number>
    latestReviews: Review[]
}

export interface ProductDetail {
    id: number
    name: string
    category: string
    originalPrice: number
    salePrice: number
    sold: number
    isHot: boolean
    limitPerUser: number | null
    mainImage: string
    images: string[]
    description: string
    specs: Record<string, string>
    seckill: SeckillInfo | null
    reviewSummary: ReviewSummary | null
}
