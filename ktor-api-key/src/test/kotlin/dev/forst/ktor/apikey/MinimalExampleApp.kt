package dev.forst.ktor.apikey

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing

/**
 * Minimal Ktor application with API Key authentication.
 */
fun Application.minimalExample() {
    // key that will be used to authenticate requests
    val expectedApiKey = "this-is-expected-key"

    // principal for the app
    data class AppPrincipal(val key: String) : Principal
    // now we install authentication feature
    install(Authentication) {
        // and then api key provider
        apiKey {
            // set function that is used to verify request
            validate { keyFromHeader ->
                keyFromHeader
                    .takeIf { it == expectedApiKey }
                    ?.let { AppPrincipal(it) }
            }
        }
    }

    routing {
        authenticate {
            get {
                val p = call.principal<AppPrincipal>()!!
                call.respondText("Key: ${p.key}")
            }
        }
    }
}
