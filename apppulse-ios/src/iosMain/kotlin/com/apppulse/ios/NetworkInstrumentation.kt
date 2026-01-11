package com.apppulse.ios

import com.apppulse.core.api.AppPulse
import com.apppulse.core.data.Attributes
import com.apppulse.core.data.EventType
import com.apppulse.core.data.Metric
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession

object NetworkInstrumentation {
    fun instrument(session: NSURLSession = NSURLSession.sharedSession): NSURLSession = session.apply {
        // Placeholder for custom URLProtocol/Delegate wiring.
    }

    fun track(request: NSURLRequest, response: NSURLResponse?, durationMs: Double, error: String?) {
        AppPulse.trackEvent(
            type = EventType.NETWORK,
            attributes = Attributes(
                mapOf(
                    "url" to (request.URL?.absoluteString ?: ""),
                    "method" to (request.HTTPMethod ?: "GET"),
                    "error" to (error ?: "")
                )
            ),
            metric = Metric(name = "durationMs", value = durationMs)
        )
    }
}
