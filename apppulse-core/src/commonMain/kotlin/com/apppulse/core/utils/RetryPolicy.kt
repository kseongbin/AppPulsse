package com.apppulse.core.utils

interface RetryPolicy {
    fun nextDelay(retryCount: Int): Long?
}

class ExponentialBackoffRetryPolicy(
    private val initialDelayMs: Long = 1_000,
    private val multiplier: Double = 2.0,
    private val maxDelayMs: Long = 15_000,
    private val maxRetries: Int = 3
) : RetryPolicy {
    override fun nextDelay(retryCount: Int): Long? {
        if (retryCount >= maxRetries) return null
        val delay = (initialDelayMs * pow(multiplier, retryCount)).toLong()
        return delay.coerceAtMost(maxDelayMs)
    }

    private fun pow(base: Double, exp: Int): Double {
        var value = 1.0
        repeat(exp) { value *= base }
        return value
    }
}
