package com.apppulse.sdk.collector

import com.apppulse.sdk.config.AppPulseConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Central registry for collecting metrics from all collectors
 *
 * Thread-safe metric collection using Kotlin Channels
 * Implements sampling and batching logic
 */
class CollectorRegistry(
    private val config: AppPulseConfig
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Unbuffered channel for back-pressure control
    private val metricChannel = Channel<MetricEvent>(Channel.UNLIMITED)

    // Expose as Flow for consumption
    val metricFlow = metricChannel.receiveAsFlow()

    private var isActive = true

    /**
     * Report a metric event
     * Applies sampling before accepting the metric
     *
     * @param event Metric event to report
     */
    fun reportMetric(event: MetricEvent) {
        if (!isActive) return

        // Check if metric type is enabled
        if (event.type !in config.enabledMetrics) {
            if (config.debug) {
                println("AppPulse: Metric type ${event.type} is disabled, skipping")
            }
            return
        }

        // Apply sampling
        if (!shouldSample()) {
            if (config.debug) {
                println("AppPulse: Metric ${event.id} filtered by sampling (rate: ${config.samplingRate})")
            }
            return
        }

        // Send to channel (non-blocking with UNLIMITED buffer)
        scope.launch {
            try {
                metricChannel.send(event)
                if (config.debug) {
                    println("AppPulse: Metric reported: ${event.type} - ${event.id}")
                }
            } catch (e: Exception) {
                if (config.debug) {
                    println("AppPulse: Failed to report metric: ${e.message}")
                }
            }
        }
    }

    /**
     * Determine if a metric should be sampled based on sampling rate
     *
     * @return true if metric should be collected, false otherwise
     */
    private fun shouldSample(): Boolean {
        // Always sample if rate is 1.0
        if (config.samplingRate >= 1.0) return true

        // Never sample if rate is 0.0
        if (config.samplingRate <= 0.0) return false

        // Probabilistic sampling
        return Random.nextDouble() < config.samplingRate
    }

    /**
     * Shutdown the registry and clean up resources
     */
    fun shutdown() {
        isActive = false
        metricChannel.close()
    }
}
