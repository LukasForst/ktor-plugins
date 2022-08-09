package dev.forst.ktor.apikey

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.pipeline.PipelineContext

const val apiKeyAuth = "api-key"

object Routes {
    const val authenticated = "authenticated"
    const val open = "open"
}

fun buildApplicationModule(
    apiKeyConfig: ApiKeyAuthenticationProvider.Configuration.() -> Unit
): ApplicationModule = {
    install(ContentNegotiation) {
        jackson()
    }

    install(Authentication) {
        apiKey(apiKeyAuth, apiKeyConfig)
    }

    routing {
        authenticate(apiKeyAuth) {
            route(Routes.authenticated) {
                get { respondPrincipal() }
                post { respondPrincipal() }
            }
        }

        route(Routes.open) {
            get { respondOk() }
            post { respondOk() }
        }
    }
}

typealias ApplicationModule = Application.() -> Unit

suspend fun PipelineContext<*, ApplicationCall>.respondOk() {
    call.respond(HttpStatusCode.OK)
}

suspend fun PipelineContext<*, ApplicationCall>.respondPrincipal() {
    val principal = call.principal<Principal>()
        ?: throw IllegalArgumentException("No Principal found!")
    call.respond(principal)
}

