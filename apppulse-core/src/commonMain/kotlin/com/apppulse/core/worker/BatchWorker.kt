package com.apppulse.core.worker

import com.apppulse.core.queue.EventQueue
import com.apppulse.core.transport.Transport
import com.apppulse.core.transport.TransportResult
import com.apppulse.core.utils.RetryPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatchWorker(
    private val queue: EventQueue,
    private val transport: Transport,
    private val retryPolicy: RetryPolicy,
    private val scope: CoroutineScope,
    private val batchSize: Int,
    private val intervalMs: Long
) {
    private var loopJob: Job? = null

    fun start() {
        if (loopJob != null) return
        loopJob = scope.launch {
            while (isActive) {
                delay(intervalMs)
                flushInternal()
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    fun scheduleFlush() {
        scope.launch {
            flushInternal()
        }
    }

    suspend fun flushNow() {
        flushInternal()
    }

    private suspend fun flushInternal() {
        if (queue.size() == 0) return
        val batch = queue.dequeueBatch(batchSize)
        if (batch.isEmpty()) return

        var attempt = 0
        while (true) {
            val result = runCatching { transport.send(batch) }
                .getOrElse { TransportResult(success = false, shouldRetry = true) }

            if (result.success) {
                return
            }

            val nextDelay = retryPolicy.nextDelay(attempt)
            if (result.shouldRetry && nextDelay != null) {
                attempt += 1
                delay(nextDelay)
            } else {
                return
            }
        }
    }
}
