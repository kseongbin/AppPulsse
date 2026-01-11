package com.apppulse.android.collector

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.apppulse.core.api.AppPulse
import com.apppulse.core.data.Attributes
import com.apppulse.core.data.EventType

/**
 * Lightweight cold/warm start tracker based on Application callbacks.
 */
class AppStartCollector(
    private val application: Application
) : Application.ActivityLifecycleCallbacks {

    private var isColdStart = true
    private var launchTimestamp = 0L
    private var firstScreenRecorded = false

    fun register() {
        launchTimestamp = SystemClock.elapsedRealtime()
        application.registerActivityLifecycleCallbacks(this)
    }

    fun unregister() {
        application.unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isColdStart) {
            AppPulse.trackEvent(
                type = EventType.APP_START,
                attributes = Attributes(mapOf("mode" to "cold"))
            )
            Log.d(TAG, "Queued cold start event. queueSize=${AppPulse.queueSize()}")
        } else {
            AppPulse.trackEvent(
                type = EventType.APP_START,
                attributes = Attributes(mapOf("mode" to "warm"))
            )
            Log.d(TAG, "Queued warm start event. queueSize=${AppPulse.queueSize()}")
        }
    }

    override fun onActivityPostResumed(activity: Activity) {
        if (!firstScreenRecorded) {
            activity.window?.decorView?.post {
                val duration = SystemClock.elapsedRealtime() - launchTimestamp
                AppPulse.trackEvent(
                    type = EventType.FIRST_SCREEN,
                    attributes = Attributes(mapOf("durationMs" to duration.toString()))
                )
                Log.d(TAG, "First screen duration=$duration ms")
                firstScreenRecorded = true
                isColdStart = false
            }
        }
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private companion object {
        const val TAG = "AppStartCollector"
    }
}
