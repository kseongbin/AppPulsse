package com.apppulse.core.queue

import com.apppulse.core.data.Event

interface EventQueue {
    fun enqueue(event: Event)
    fun dequeueBatch(maxItems: Int): List<Event>
    fun size(): Int
}

class InMemoryEventQueue(
    private val maxSize: Int
) : EventQueue {
    private val items = ArrayDeque<Event>()

    override fun enqueue(event: Event) {
        if (items.size >= maxSize) {
            items.removeFirstOrNull()
        }
        items.addLast(event)
    }

    override fun dequeueBatch(maxItems: Int): List<Event> {
        if (items.isEmpty()) return emptyList()
        val batch = mutableListOf<Event>()
        repeat(maxItems.coerceAtMost(items.size)) {
            items.removeFirstOrNull()?.let(batch::add)
        }
        return batch
    }

    override fun size(): Int = items.size
}
