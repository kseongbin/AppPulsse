package com.apppulse.core

import com.apppulse.core.config.AppPulseConfig
import com.apppulse.core.data.EventType
import com.apppulse.core.utils.DefaultSampler
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SamplerTest {
    private val config = AppPulseConfig(
        apiKey = "demo",
        endpoint = "https://collector.example.com",
        networkSamplingRate = 0.0,
        frameSamplingRate = 0.0
    )

    @Test
    fun `app start events are always collected`() {
        val sampler = DefaultSampler(config)
        repeat(10) {
            assertTrue(sampler.shouldSample(EventType.APP_START))
        }
    }

    @Test
    fun `network sampling honors configured rate`() {
        val sampler = DefaultSampler(config)
        repeat(10) {
            assertFalse(sampler.shouldSample(EventType.NETWORK))
        }
    }
}
