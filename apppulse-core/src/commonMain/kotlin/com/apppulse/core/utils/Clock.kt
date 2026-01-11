package com.apppulse.core.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface AppPulseClock {
    fun now(): Instant
}

object SystemClock : AppPulseClock {
    override fun now(): Instant = Clock.System.now()
}
