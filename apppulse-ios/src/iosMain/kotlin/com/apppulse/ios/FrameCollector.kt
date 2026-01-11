package com.apppulse.ios

import com.apppulse.core.api.AppPulse
import com.apppulse.core.data.Attributes
import com.apppulse.core.data.EventType
import platform.CoreAnimation.CADisplayLink
import platform.Foundation.NSRunLoop
import platform.Foundation.NSRunLoopCommonModes
import platform.Foundation.NSSelectorFromString

class IOSFrameCollector {
    private var displayLink: CADisplayLink? = null
    private var sampleCount = 0
    private var jankyFrames = 0

    fun start() {
        displayLink = CADisplayLink.displayLinkWithTarget(this, selector = NSSelectorFromString("tick"))
        displayLink?.addToRunLoop(NSRunLoop.mainRunLoop(), NSRunLoopCommonModes)
    }

    fun stop() {
        displayLink?.invalidate()
        displayLink = null
    }

    @Suppress("UNUSED_PARAMETER")
    fun tick(link: CADisplayLink) {
        sampleCount += 1
        val duration = link.duration * 1000.0
        if (duration > 18.0) {
            jankyFrames += 1
        }
        if (sampleCount >= 60) {
            AppPulse.trackEvent(
                type = EventType.FRAME,
                attributes = Attributes(
                    mapOf(
                        "sampleCount" to sampleCount.toString(),
                        "jankyFrames" to jankyFrames.toString()
                    )
                )
            )
            sampleCount = 0
            jankyFrames = 0
        }
    }
}
