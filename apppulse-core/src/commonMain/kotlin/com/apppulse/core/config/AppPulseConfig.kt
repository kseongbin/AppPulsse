package com.apppulse.core.config

/**
 * Configuration for AppPulse SDK collectors and transport.
 */
data class AppPulseConfig(
    val apiKey: String,
    val endpoint: String,
    val batchSize: Int = 20,
    val batchIntervalMs: Long = 10_000,
    val maxEventsPerSession: Int = 200,
    val queueMaxSize: Int = 1_000,
    val networkSamplingRate: Double = 0.10,
    val frameSamplingRate: Double = 0.0,
    val frameSummaryWindowMs: Long = 5_000,
    val debugLogging: Boolean = false
) {
    val appStartSamplingRate: Double = 1.0
    val firstScreenSamplingRate: Double = 1.0

    init {
        require(apiKey.isNotBlank()) { "API key must not be blank" }
        require(endpoint.startsWith("https://")) { "Endpoint must use https" }
        require(batchSize > 0) { "Batch size must be positive" }
        require(batchIntervalMs > 0) { "Batch interval must be positive" }
        require(maxEventsPerSession > 0) { "Max events must be positive" }
        require(queueMaxSize >= batchSize) { "Queue must hold at least one batch" }
        require(networkSamplingRate in 0.0..1.0) { "Sampling rate must be 0-1" }
        require(frameSamplingRate in 0.0..1.0) { "Frame sampling rate must be 0-1" }
    }
}
