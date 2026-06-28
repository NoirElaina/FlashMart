export function getOrderPaymentRemainingSeconds(payExpireTime: string, now = Date.now()) {
    const normalizedPayExpireTime = payExpireTime.replace(' ', 'T')
    const deadlineTimestamp = Date.parse(normalizedPayExpireTime)

    if (Number.isNaN(deadlineTimestamp)) {
        return null
    }

    return Math.max(0, Math.ceil((deadlineTimestamp - now) / 1000))
}

export function formatOrderCountdown(seconds: number) {
    const minutes = Math.floor(seconds / 60)
    const restSeconds = seconds % 60

    return `${String(minutes).padStart(2, '0')}:${String(restSeconds).padStart(2, '0')}`
}
