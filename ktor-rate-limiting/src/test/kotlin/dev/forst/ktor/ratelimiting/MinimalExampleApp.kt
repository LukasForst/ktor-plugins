package dev.forst.ktor.ratelimiting

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.time.Duration

/**
 * Minimal Ktor application with Rate Limiting enabled.
 */
fun Application.minimalExample() {
    // install feature
    install(RateLimiting) {
        registerLimit(
            // allow 10 requests
            limit = 10,
            // each 1 minute
            window = Duration.ofMinutes(1)
        ) {
            // use host as a key to determine who is who
            request.origin.host
        }
        // and exclude path which ends with "excluded"
        excludeRequestWhen {
            request.path().endsWith("excluded")
        }
    }
    // now add some routes
    routing {
        get {
            call.respondText("Hello ${call.request.origin.host}")
        }
        get("excluded") {
            call.respondText("Hello ${call.request.origin.host}")
        }
    }
}
