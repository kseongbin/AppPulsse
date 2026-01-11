package com.apppulse.sdk.config

import kotlinx.serialization.Serializable

/**
 * Configuration for AppPulse SDK
 *
 * @property apiKey API key for authentication
 * @property endpoint Server endpoint URL for metric uploads
 * @property samplingRate Sampling rate (0.0 to 1.0) - percentage of metrics to collect
 * @property uploadIntervalMs Interval between uploads in milliseconds
 * @property enabledMetrics Set of metric types to collect
 * @property debug Enable debug logging
 * @property maxStorageBytes Maximum local storage size in bytes (default 5MB)
 */
@Serializable
data class AppPulseConfig(
    val apiKey: String,
    val endpoint: String,
    val samplingRate: Double = 1.0,
    val uploadIntervalMs: Long = 300_000, // 5 minutes
    val enabledMetrics: Set<MetricType> = setOf(
        MetricType.STARTUP,
        MetricType.NETWORK,
        MetricType.UI_PERFORMANCE
    ),
    val debug: Boolean = false,
    val maxStorageBytes: Long = 5 * 1024 * 1024 // 5MB
) {
    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        require(endpoint.isNotBlank()) { "Endpoint cannot be blank" }
        require(endpoint.startsWith("https://")) { "Endpoint must use HTTPS" }
        require(samplingRate in 0.0..1.0) { "Sampling rate must be between 0.0 and 1.0" }
        require(uploadIntervalMs > 0) { "Upload interval must be positive" }
        require(maxStorageBytes > 0) { "Max storage bytes must be positive" }
        require(enabledMetrics.isNotEmpty()) { "At least one metric type must be enabled" }
    }
}
