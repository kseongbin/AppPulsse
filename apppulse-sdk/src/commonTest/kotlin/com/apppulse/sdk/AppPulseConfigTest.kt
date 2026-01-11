package com.apppulse.sdk

import com.apppulse.sdk.config.AppPulseConfig
import com.apppulse.sdk.config.MetricType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppPulseConfigTest {

    @Test
    fun `valid config should be created successfully`() {
        val config = AppPulseConfig(
            apiKey = "test-api-key",
            endpoint = "https://api.example.com/metrics",
            samplingRate = 0.5,
            uploadIntervalMs = 60_000
        )

        assertEquals("test-api-key", config.apiKey)
        assertEquals("https://api.example.com/metrics", config.endpoint)
        assertEquals(0.5, config.samplingRate)
        assertEquals(60_000, config.uploadIntervalMs)
    }

    @Test
    fun `blank apiKey should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            AppPulseConfig(
                apiKey = "",
                endpoint = "https://api.example.com/metrics"
            )
        }
    }

    @Test
    fun `blank endpoint should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            AppPulseConfig(
                apiKey = "test-key",
                endpoint = ""
            )
        }
    }

    @Test
    fun `non-https endpoint should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            AppPulseConfig(
                apiKey = "test-key",
                endpoint = "http://api.example.com/metrics"
            )
        }
    }

    @Test
    fun `samplingRate below 0 should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            AppPulseConfig(
                apiKey = "test-key",
                endpoint = "https://api.example.com/metrics",
                samplingRate = -0.1
            )
        }
    }

    @Test
    fun `samplingRate above 1 should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            AppPulseConfig(
                apiKey = "test-key",
                endpoint = "https://api.example.com/metrics",
                samplingRate = 1.1
            )
        }
    }

    @Test
    fun `negative uploadIntervalMs should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            AppPulseConfig(
                apiKey = "test-key",
                endpoint = "https://api.example.com/metrics",
                uploadIntervalMs = -1000
            )
        }
    }

    @Test
    fun `empty enabledMetrics should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            AppPulseConfig(
                apiKey = "test-key",
                endpoint = "https://api.example.com/metrics",
                enabledMetrics = emptySet()
            )
        }
    }

    @Test
    fun `default values should be set correctly`() {
        val config = AppPulseConfig(
            apiKey = "test-key",
            endpoint = "https://api.example.com/metrics"
        )

        assertEquals(1.0, config.samplingRate)
        assertEquals(300_000, config.uploadIntervalMs)
        assertEquals(5 * 1024 * 1024, config.maxStorageBytes)
        assertTrue(config.enabledMetrics.containsAll(
            setOf(MetricType.STARTUP, MetricType.NETWORK, MetricType.UI_PERFORMANCE)
        ))
        assertEquals(false, config.debug)
    }
}
