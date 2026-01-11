package com.apppulse.sdk.config

import kotlinx.serialization.Serializable

/**
 * Types of metrics that can be collected by AppPulse SDK
 */
@Serializable
enum class MetricType {
    /**
     * App startup time metrics (cold/warm/hot launch)
     */
    STARTUP,

    /**
     * Network performance metrics (API response times, errors, bandwidth)
     */
    NETWORK,

    /**
     * UI rendering performance (FPS, frame drops, jank)
     */
    UI_PERFORMANCE,

    /**
     * Custom application-specific metrics
     */
    CUSTOM
}
