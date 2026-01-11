package com.apppulse.core

import com.apppulse.core.utils.ExponentialBackoffRetryPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RetryPolicyTest {
    @Test
    fun `exponential backoff stops after max retries`() {
        val policy = ExponentialBackoffRetryPolicy(initialDelayMs = 100, maxRetries = 2)
        assertEquals(100, policy.nextDelay(0))
        assertEquals(200, policy.nextDelay(1))
        assertNull(policy.nextDelay(2))
    }
}
