package com.apppulse.sdk.collector

import com.apppulse.sdk.config.MetricType
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Base sealed class for all metric events
 */
@Serializable
sealed class MetricEvent {
    abstract val id: String
    abstract val timestamp: Instant
    abstract val sessionId: String
    abstract val type: MetricType

    /**
     * App startup time metric
     *
     * @property duration Startup duration in milliseconds
     * @property startupType Type of startup (COLD, WARM, HOT)
     */
    @Serializable
    data class StartupMetric(
        override val id: String,
        override val timestamp: Instant,
        override val sessionId: String,
        val duration: Long,
        val startupType: StartupType
    ) : MetricEvent() {
        override val type: MetricType = MetricType.STARTUP

        @Serializable
        enum class StartupType {
            /** App process created from scratch */
            COLD,
            /** Activity recreated but process exists */
            WARM,
            /** Activity brought to foreground */
            HOT
        }
    }

    /**
     * Network request metric
     *
     * @property url Request URL (sanitized)
     * @property method HTTP method (GET, POST, etc.)
     * @property statusCode HTTP status code (null if failed before response)
     * @property duration Request duration in milliseconds
     * @property requestSize Request payload size in bytes (null if not available)
     * @property responseSize Response payload size in bytes (null if not available)
     * @property error Error message if request failed (null if successful)
     */
    @Serializable
    data class NetworkMetric(
        override val id: String,
        override val timestamp: Instant,
        override val sessionId: String,
        val url: String,
        val method: String,
        val statusCode: Int?,
        val duration: Long,
        val requestSize: Long?,
        val responseSize: Long?,
        val error: String?
    ) : MetricEvent() {
        override val type: MetricType = MetricType.NETWORK
    }

    /**
     * UI rendering performance metric
     *
     * @property avgFrameTime Average frame rendering time in milliseconds
     * @property jankCount Number of janky frames (>16.67ms for 60fps)
     * @property droppedFrames Number of completely dropped frames
     * @property sampleSize Number of frames in this sample
     * @property screenName Screen/view name where metrics were collected (optional)
     */
    @Serializable
    data class UIPerformanceMetric(
        override val id: String,
        override val timestamp: Instant,
        override val sessionId: String,
        val avgFrameTime: Double,
        val jankCount: Int,
        val droppedFrames: Int,
        val sampleSize: Int,
        val screenName: String? = null
    ) : MetricEvent() {
        override val type: MetricType = MetricType.UI_PERFORMANCE
    }

    /**
     * Custom application metric
     *
     * @property name Metric name
     * @property value Numeric value
     * @property metadata Additional key-value metadata
     */
    @Serializable
    data class CustomMetric(
        override val id: String,
        override val timestamp: Instant,
        override val sessionId: String,
        val name: String,
        val value: Double,
        val metadata: Map<String, String> = emptyMap()
    ) : MetricEvent() {
        override val type: MetricType = MetricType.CUSTOM
    }
}
