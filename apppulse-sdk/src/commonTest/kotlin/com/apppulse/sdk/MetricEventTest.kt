package com.apppulse.sdk

import com.apppulse.sdk.collector.MetricEvent
import com.apppulse.sdk.config.MetricType
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MetricEventTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun `StartupMetric should serialize and deserialize correctly`() {
        val metric = MetricEvent.StartupMetric(
            id = "startup-123",
            timestamp = Clock.System.now(),
            sessionId = "session-456",
            duration = 1500,
            startupType = MetricEvent.StartupMetric.StartupType.COLD
        )

        val serialized = json.encodeToString(metric)
        val deserialized = json.decodeFromString<MetricEvent.StartupMetric>(serialized)

        assertEquals(metric.id, deserialized.id)
        assertEquals(metric.duration, deserialized.duration)
        assertEquals(metric.startupType, deserialized.startupType)
        assertEquals(MetricType.STARTUP, deserialized.type)
    }

    @Test
    fun `NetworkMetric should serialize and deserialize correctly`() {
        val metric = MetricEvent.NetworkMetric(
            id = "network-123",
            timestamp = Clock.System.now(),
            sessionId = "session-456",
            url = "https://api.example.com/users",
            method = "GET",
            statusCode = 200,
            duration = 350,
            requestSize = 1024,
            responseSize = 2048,
            error = null
        )

        val serialized = json.encodeToString(metric)
        val deserialized = json.decodeFromString<MetricEvent.NetworkMetric>(serialized)

        assertEquals(metric.id, deserialized.id)
        assertEquals(metric.url, deserialized.url)
        assertEquals(metric.method, deserialized.method)
        assertEquals(metric.statusCode, deserialized.statusCode)
        assertEquals(metric.duration, deserialized.duration)
        assertEquals(MetricType.NETWORK, deserialized.type)
    }

    @Test
    fun `NetworkMetric with error should serialize correctly`() {
        val metric = MetricEvent.NetworkMetric(
            id = "network-error-123",
            timestamp = Clock.System.now(),
            sessionId = "session-456",
            url = "https://api.example.com/users",
            method = "POST",
            statusCode = null,
            duration = 5000,
            requestSize = 512,
            responseSize = null,
            error = "Connection timeout"
        )

        val serialized = json.encodeToString(metric)
        val deserialized = json.decodeFromString<MetricEvent.NetworkMetric>(serialized)

        assertEquals(metric.error, deserialized.error)
        assertEquals(null, deserialized.statusCode)
        assertEquals(null, deserialized.responseSize)
    }

    @Test
    fun `UIPerformanceMetric should serialize and deserialize correctly`() {
        val metric = MetricEvent.UIPerformanceMetric(
            id = "ui-perf-123",
            timestamp = Clock.System.now(),
            sessionId = "session-456",
            avgFrameTime = 14.5,
            jankCount = 5,
            droppedFrames = 2,
            sampleSize = 100,
            screenName = "HomeScreen"
        )

        val serialized = json.encodeToString(metric)
        val deserialized = json.decodeFromString<MetricEvent.UIPerformanceMetric>(serialized)

        assertEquals(metric.id, deserialized.id)
        assertEquals(metric.avgFrameTime, deserialized.avgFrameTime)
        assertEquals(metric.jankCount, deserialized.jankCount)
        assertEquals(metric.droppedFrames, deserialized.droppedFrames)
        assertEquals(metric.sampleSize, deserialized.sampleSize)
        assertEquals(metric.screenName, deserialized.screenName)
        assertEquals(MetricType.UI_PERFORMANCE, deserialized.type)
    }

    @Test
    fun `CustomMetric should serialize and deserialize correctly`() {
        val metric = MetricEvent.CustomMetric(
            id = "custom-123",
            timestamp = Clock.System.now(),
            sessionId = "session-456",
            name = "checkout_completed",
            value = 99.99,
            metadata = mapOf("currency" to "USD", "items" to "3")
        )

        val serialized = json.encodeToString(metric)
        val deserialized = json.decodeFromString<MetricEvent.CustomMetric>(serialized)

        assertEquals(metric.id, deserialized.id)
        assertEquals(metric.name, deserialized.name)
        assertEquals(metric.value, deserialized.value)
        assertEquals(metric.metadata, deserialized.metadata)
        assertEquals(MetricType.CUSTOM, deserialized.type)
    }

    @Test
    fun `MetricEvent types should have correct type property`() {
        val startupMetric = MetricEvent.StartupMetric(
            id = "1",
            timestamp = Clock.System.now(),
            sessionId = "s1",
            duration = 1000,
            startupType = MetricEvent.StartupMetric.StartupType.COLD
        )

        val networkMetric = MetricEvent.NetworkMetric(
            id = "2",
            timestamp = Clock.System.now(),
            sessionId = "s1",
            url = "https://api.example.com",
            method = "GET",
            statusCode = 200,
            duration = 500,
            requestSize = null,
            responseSize = null,
            error = null
        )

        assertEquals(MetricType.STARTUP, startupMetric.type)
        assertEquals(MetricType.NETWORK, networkMetric.type)

        // Verify individual serialization works
        val startupSerialized = json.encodeToString(startupMetric)
        val networkSerialized = json.encodeToString(networkMetric)

        assertNotNull(startupSerialized)
        assertNotNull(networkSerialized)
        assertTrue(startupSerialized.isNotEmpty())
        assertTrue(networkSerialized.isNotEmpty())
    }
}
