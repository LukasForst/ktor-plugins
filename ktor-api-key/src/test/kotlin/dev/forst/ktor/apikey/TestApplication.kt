package dev.forst.ktor.apikey

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
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
