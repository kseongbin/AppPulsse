package com.apppulse.core.transport

import com.apppulse.core.data.Event

interface Transport {
    suspend fun send(batch: List<Event>): TransportResult
}

data class TransportResult(
    val success: Boolean,
    val shouldRetry: Boolean = false
)
