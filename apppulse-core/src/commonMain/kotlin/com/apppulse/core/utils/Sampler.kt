package com.apppulse.core.utils

import com.apppulse.core.config.AppPulseConfig
import com.apppulse.core.data.EventType
import kotlin.random.Random

interface Sampler {
    fun shouldSample(type: EventType): Boolean
}

class DefaultSampler(
    private val config: AppPulseConfig,
    private val random: Random = Random.Default
) : Sampler {
    override fun shouldSample(type: EventType): Boolean = when (type) {
        EventType.APP_START, EventType.FIRST_SCREEN -> random.nextDouble() < config.appStartSamplingRate
        EventType.NETWORK -> random.nextDouble() < config.networkSamplingRate
        EventType.FRAME -> random.nextDouble() < config.frameSamplingRate
        else -> true
    }
}
