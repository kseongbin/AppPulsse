package com.apppulse.sample

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.apppulse.android.collector.AppStartCollector
import com.apppulse.android.frame.FrameMetricsCollector
import com.apppulse.core.api.AppPulse
import com.apppulse.core.config.AppPulseConfig
import com.apppulse.core.transport.Transport
import com.apppulse.core.transport.TransportResult
import com.apppulse.core.data.Event
import kotlinx.coroutines.delay

class SampleApplication : Application() {

    private lateinit var startCollector: AppStartCollector
    private lateinit var frameCollector: FrameMetricsCollector

    override fun onCreate() {
        super.onCreate()

        val config = AppPulseConfig(
            apiKey = "demo-key",
            endpoint = "https://collector.example.com",
            frameSamplingRate = 0.2
        )

        val appPulseEnabled = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0

        AppPulse.init(
            config = config,
            transport = ConsoleTransport(),
            enabled = appPulseEnabled
        )

        if (appPulseEnabled) {
            AppPulse.setUserId("sample-user")

            startCollector = AppStartCollector(this)
            startCollector.register()

            frameCollector = FrameMetricsCollector(this, enabled = config.frameSamplingRate > 0.0)
            frameCollector.register()
        }

        Log.d(
            "SampleApp",
            "AppPulse enabled=$appPulseEnabled queue size=${AppPulse.queueSize()}"
        )
    }

    private class ConsoleTransport : Transport {
        override suspend fun send(batch: List<Event>): TransportResult {
            Log.d("SampleApp", "Sending batch size=${batch.size}")
            // Pretend network latency
            delay(50)
            return TransportResult(success = true)
        }
    }
}
