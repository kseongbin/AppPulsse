package com.apppulse.core

import com.apppulse.core.data.Attributes
import com.apppulse.core.data.Event
import com.apppulse.core.data.EventType
import com.apppulse.core.queue.InMemoryEventQueue
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryEventQueueTest {
    @Test
    fun `queue drops oldest when capacity exceeded`() {
        val queue = InMemoryEventQueue(maxSize = 3)
        repeat(5) { index ->
            queue.enqueue(
                Event(
                    id = index.toString(),
                    sessionId = "session",
                    userId = null,
                    type = EventType.CUSTOM,
                    timestamp = Clock.System.now(),
                    attributes = Attributes()
                )
            )
        }

        assertEquals(3, queue.size())
        val batch = queue.dequeueBatch(3)
        assertEquals(listOf("2", "3", "4"), batch.map { it.id })
    }
}
