package dev.forst.ktor.csp

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Minimal Ktor application using Content Security Policy.
 */
fun Application.minimalExample() {
    // this sets Content-Security-Policy
    install(ContentSecurityPolicy) {
        policy { call, body ->
            when (call.request.path()) {
                "/specific" -> mapOf("default-src" to "'none'")
                "/ignored" -> null
                else -> mapOf("default-src" to "'self'")
            }
        }
    }
    // basic routing
    routing {
        get("/specific") { call.respond(HttpStatusCode.OK) }
        get("/ignored") { call.respond(HttpStatusCode.OK) }
        get("/") { call.respond(HttpStatusCode.OK) }
    }
}
