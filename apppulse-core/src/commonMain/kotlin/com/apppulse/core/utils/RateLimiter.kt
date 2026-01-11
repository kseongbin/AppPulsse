package com.apppulse.core.utils

import kotlinx.datetime.Clock

interface RateLimiter {
    fun tryConsume(): Boolean
    fun reset()
}

class SessionRateLimiter(
    private val maxEvents: Int,
    private val clock: Clock = Clock.System
) : RateLimiter {
    private var consumed = 0
    private var sessionStartedAt = clock.now()

    override fun tryConsume(): Boolean {
        if (consumed >= maxEvents) {
            return false
        }
        consumed += 1
        return true
    }

    override fun reset() {
        consumed = 0
        sessionStartedAt = clock.now()
    }

    fun startedAt() = sessionStartedAt
}
