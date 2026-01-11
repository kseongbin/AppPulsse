package com.apppulse.android.network

import com.apppulse.core.api.AppPulse
import com.apppulse.core.data.Attributes
import com.apppulse.core.data.EventType
import com.apppulse.core.data.Metric
import okhttp3.Interceptor
import okhttp3.Response

class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startNs = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            track(request.url.toString(), request.method, response.code, System.nanoTime() - startNs, null)
            response
        } catch (t: Throwable) {
            track(request.url.toString(), request.method, null, System.nanoTime() - startNs, t.message)
            throw t
        }
    }

    private fun track(url: String, method: String, statusCode: Int?, durationNs: Long, error: String?) {
        AppPulse.trackEvent(
            type = EventType.NETWORK,
            attributes = Attributes(
                mapOf(
                    "url" to url.take(128),
                    "method" to method,
                    "status" to statusCode?.toString().orEmpty(),
                    "error" to (error ?: "")
                )
            ),
            metric = Metric(name = "durationMs", value = durationNs / 1_000_000.0)
        )
    }
}
