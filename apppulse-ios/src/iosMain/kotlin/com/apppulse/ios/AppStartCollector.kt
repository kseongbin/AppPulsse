package com.apppulse.ios

import com.apppulse.core.api.AppPulse
import com.apppulse.core.data.Attributes
import com.apppulse.core.data.EventType
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSObjectProtocol
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidFinishLaunchingNotification

class IOSAppStartCollector {
    private val observers = mutableListOf<NSObjectProtocol?>()

    fun start() {
        val center = NSNotificationCenter.defaultCenter
        observers += center.addObserverForName(
            name = UIApplicationDidFinishLaunchingNotification,
            `object` = null,
            queue = null
        ) { _: NSNotification? ->
            AppPulse.trackEvent(EventType.APP_START, Attributes(mapOf("platform" to "ios")))
        }
        observers += center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) { _: NSNotification? ->
            AppPulse.trackEvent(EventType.FIRST_SCREEN, Attributes(mapOf("stage" to "active")))
        }
    }

    fun stop() {
        val center = NSNotificationCenter.defaultCenter
        observers.forEach { it?.let(center::removeObserver) }
        observers.clear()
    }
}
