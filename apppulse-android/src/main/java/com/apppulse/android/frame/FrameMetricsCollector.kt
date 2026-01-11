package com.apppulse.android.frame

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.FrameMetrics
import android.view.Window
import androidx.annotation.RequiresApi
import com.apppulse.core.api.AppPulse
import com.apppulse.core.data.Attributes
import com.apppulse.core.data.EventType

class FrameMetricsCollector(
    private val application: Application,
    private val enabled: Boolean
) : Application.ActivityLifecycleCallbacks {

    private val listeners = mutableMapOf<Window, Window.OnFrameMetricsAvailableListener>()

    fun register() {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            application.registerActivityLifecycleCallbacks(this)
        }
    }

    fun unregister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            listeners.forEach { (window, listener) -> window.removeOnFrameMetricsAvailableListener(listener) }
            listeners.clear()
            application.unregisterActivityLifecycleCallbacks(this)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) {
        if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val window = activity.window ?: return
        val listener = FrameListener()
        window.addOnFrameMetricsAvailableListener(listener, null)
        listeners[window] = listener
    }

    override fun onActivityStopped(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val window = activity.window ?: return
        listeners.remove(window)?.let { window.removeOnFrameMetricsAvailableListener(it) }
    }

    override fun onActivityDestroyed(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    @RequiresApi(Build.VERSION_CODES.N)
    private inner class FrameListener : Window.OnFrameMetricsAvailableListener {
        private var totalFrames = 0
        private var jankyFrames = 0

        override fun onFrameMetricsAvailable(window: Window, frameMetrics: FrameMetrics, dropCountSinceLastInvocation: Int) {
            totalFrames += 1
            val totalDuration = frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)
            if (totalDuration > 16_000_000L) {
                jankyFrames += 1
            }
            if (totalFrames >= 60) {
                report(totalFrames, jankyFrames)
                totalFrames = 0
                jankyFrames = 0
            }
        }

        private fun report(samples: Int, janks: Int) {
            AppPulse.trackEvent(
                type = EventType.FRAME,
                attributes = Attributes(
                    mapOf(
                        "sampleCount" to samples.toString(),
                        "jankyFrames" to janks.toString()
                    )
                )
            )
        }
    }
}
