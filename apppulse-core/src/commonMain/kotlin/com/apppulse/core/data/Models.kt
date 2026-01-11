package com.apppulse.core.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class EventType {
    APP_START,
    FIRST_SCREEN,
    NETWORK,
    FRAME,
    CUSTOM,
    SPAN_START,
    SPAN_END
}

@Serializable
data class Metric(
    val name: String,
    val value: Double,
    val unit: String? = null
)

@Serializable
data class Attributes(
    val values: Map<String, String> = emptyMap()
) {
    fun merge(other: Attributes): Attributes = Attributes(values + other.values)
    fun withDefaults(defaults: Map<String, String>): Attributes = Attributes(defaults + values)
}

@Serializable
data class Event(
    val id: String,
    val sessionId: String,
    val userId: String?,
    val type: EventType,
    val timestamp: Instant,
    val attributes: Attributes = Attributes(),
    val metric: Metric? = null
)

@Serializable
data class Span(
    val id: String,
    val parentId: String?,
    val name: String,
    val start: Instant,
    val end: Instant? = null,
    val attributes: Attributes = Attributes()
)
