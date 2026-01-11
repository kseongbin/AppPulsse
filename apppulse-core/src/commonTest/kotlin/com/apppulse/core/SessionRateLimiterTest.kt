package com.apppulse.core

import com.apppulse.core.utils.SessionRateLimiter
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionRateLimiterTest {
    @Test
    fun `rate limiter blocks after max events`() {
        val limiter = SessionRateLimiter(maxEvents = 3)
        assertTrue(limiter.tryConsume())
        assertTrue(limiter.tryConsume())
        assertTrue(limiter.tryConsume())
        assertFalse(limiter.tryConsume())
        limiter.reset()
        assertTrue(limiter.tryConsume())
    }
}
