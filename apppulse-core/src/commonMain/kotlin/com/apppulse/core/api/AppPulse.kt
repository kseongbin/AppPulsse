package com.apppulse.core.api

import com.apppulse.core.config.AppPulseConfig
import com.apppulse.core.data.Attributes
import com.apppulse.core.data.Event
import com.apppulse.core.data.EventType
import com.apppulse.core.data.Metric
import com.apppulse.core.data.Span
import com.apppulse.core.queue.EventQueue
import com.apppulse.core.queue.InMemoryEventQueue
import com.apppulse.core.transport.Transport
import com.apppulse.core.utils.AppPulseClock
import com.apppulse.core.utils.DefaultSampler
import com.apppulse.core.utils.DefaultUuidGenerator
import com.apppulse.core.utils.ExponentialBackoffRetryPolicy
import com.apppulse.core.utils.RateLimiter
import com.apppulse.core.utils.RetryPolicy
import com.apppulse.core.utils.Sampler
import com.apppulse.core.utils.SessionRateLimiter
import com.apppulse.core.utils.SystemClock
import com.apppulse.core.utils.UuidGenerator
import com.apppulse.core.worker.BatchWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.Volatile

object AppPulse {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var initialized = false

    private lateinit var config: AppPulseConfig
    private lateinit var queue: EventQueue
    private lateinit var transport: Transport
    private lateinit var sampler: Sampler
    private lateinit var rateLimiter: RateLimiter
    private lateinit var retryPolicy: RetryPolicy
    private lateinit var worker: BatchWorker
    private lateinit var clock: AppPulseClock
    private lateinit var uuidGenerator: UuidGenerator

    private var sessionId: String = ""
    private var userId: String? = null
    private val spans = mutableMapOf<String, Span>()

    fun init(
        config: AppPulseConfig,
        transport: Transport,
        queue: EventQueue = InMemoryEventQueue(config.queueMaxSize),
        sampler: Sampler = DefaultSampler(config),
        rateLimiter: RateLimiter = SessionRateLimiter(config.maxEventsPerSession),
        retryPolicy: RetryPolicy = ExponentialBackoffRetryPolicy(),
        clock: AppPulseClock = SystemClock,
        uuidGenerator: UuidGenerator = DefaultUuidGenerator(),
        enabled: Boolean = true
    ) {
        runCatching {
            shutdownWorker()
            if (!enabled) {
                initialized = false
                return
            }
            this.config = config
            this.transport = transport
            this.queue = queue
            this.sampler = sampler
            this.rateLimiter = rateLimiter
            this.retryPolicy = retryPolicy
            this.clock = clock
            this.uuidGenerator = uuidGenerator
            this.sessionId = uuidGenerator.random()
            this.worker = BatchWorker(
                queue = this.queue,
                transport = this.transport,
                retryPolicy = this.retryPolicy,
                scope = scope,
                batchSize = config.batchSize,
                intervalMs = config.batchIntervalMs
            )
            this.worker.start()
            this.rateLimiter.reset()
            initialized = true
        }
    }

    fun setUserId(id: String?) {
        userId = id
    }

    fun trackEvent(
        type: EventType,
        attributes: Attributes = Attributes(),
        metric: Metric? = null
    ) {
        if (!initialized) return
        runCatching {
            if (!sampler.shouldSample(type)) return
            if (!rateLimiter.tryConsume()) return
            val event = Event(
                id = uuidGenerator.random(),
                sessionId = sessionId,
                userId = userId,
                type = type,
                timestamp = clock.now(),
                attributes = attributes,
                metric = metric
            )
            queue.enqueue(event)
            worker.scheduleFlush()
        }
    }

    fun startSpan(name: String, attributes: Attributes = Attributes(), parentId: String? = null): String {
        if (!initialized) return ""
        val spanId = uuidGenerator.random()
        runCatching {
            val span = Span(
                id = spanId,
                parentId = parentId,
                name = name,
                start = clock.now(),
                attributes = attributes
            )
            spans[spanId] = span
            trackEvent(EventType.SPAN_START, attributes.merge(Attributes(mapOf("span.name" to name))))
        }
        return spanId
    }

    fun endSpan(spanId: String, attributes: Attributes = Attributes()) {
        if (!initialized) return
        runCatching {
            val span = spans.remove(spanId) ?: return
            val merged = span.attributes.merge(attributes)
            trackEvent(
                type = EventType.SPAN_END,
                attributes = merged.merge(Attributes(mapOf("span.durationMs" to spanDurationMs(span).toString())))
            )
        }
    }

    fun flush() {
        if (!initialized) return
        runBlocking {
            worker.flushNow()
        }
    }

    fun queueSize(): Int = runCatching { queue.size() }.getOrDefault(0)

    private fun shutdownWorker() {
        if (::worker.isInitialized) {
            worker.stop()
        }
    }

    private fun spanDurationMs(span: Span): Long {
        val end = clock.now()
        val duration = end.toEpochMilliseconds() - span.start.toEpochMilliseconds()
        return if (duration >= 0) duration else 0L
    }
}
