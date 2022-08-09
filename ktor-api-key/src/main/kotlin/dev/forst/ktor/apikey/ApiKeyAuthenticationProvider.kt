package dev.forst.ktor.apikey

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.Authentication
import io.ktor.auth.AuthenticationContext
import io.ktor.auth.AuthenticationFailedCause
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.AuthenticationProcedureChallenge
import io.ktor.auth.AuthenticationProvider
import io.ktor.auth.Principal
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineInterceptor

/**
 * Represents an API Key authentication provider.
 * @property name is the name of the provider, or `null` for a default provider.
 */
class ApiKeyAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {
    internal val headerName: String = configuration.headerName

    internal val authenticationFunction = configuration.authenticationFunction

    internal val challengeFunction = configuration.challengeFunction

    internal val authScheme = configuration.authScheme

    /**
     * Api key auth configuration.
     */
    class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name) {

        internal lateinit var authenticationFunction: ApiKeyAuthenticationFunction

        internal var challengeFunction: ApiKeyAuthChallengeFunction = {
            call.respond(HttpStatusCode.Unauthorized)
        }

        /**
         * Name of the scheme used when challenge fails, see [AuthenticationContext.challenge].
         */
        var authScheme: String = "apiKey"

        /**
         * Name of the header that will be used as a source for the api key.
         */
        var headerName: String = "X-Api-Key"

        /**
         * Sets a validation function that will check given API key retrieved from [headerName] instance and return [Principal],
         * or null if credential does not correspond to an authenticated principal.
         */
        fun validate(body: suspend ApplicationCall.(String) -> Principal?) {
            authenticationFunction = body
        }

        /**
         * A response to send back if authentication failed.
         */
        fun challenge(body: ApiKeyAuthChallengeFunction) {
            challengeFunction = body
        }
    }
}

/**
 * Installs API Key authentication mechanism.
 */
fun Authentication.Configuration.apiKey(
    name: String? = null,
    configure: ApiKeyAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = ApiKeyAuthenticationProvider(ApiKeyAuthenticationProvider.Configuration(name).apply(configure))
    val headerName = provider.headerName
    val authenticate = provider.authenticationFunction
    val authScheme = provider.authScheme
    val challenge = provider.challengeFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val apiKey = call.request.header(headerName)
        val principal = apiKey?.let { authenticate(call, it) }

        val cause = when {
            apiKey == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }
        if (cause != null) {
            context.challenge(authScheme, cause) {
                challenge.invoke(this, it)
                it.complete()
            }
        }
        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}

/**
 * Alias for function signature that is invoked when verifying header.
 */
typealias ApiKeyAuthenticationFunction = suspend ApplicationCall.(String) -> Principal?

/**
 * Alias for function signature that is called when authentication fails.
 */
typealias ApiKeyAuthChallengeFunction = PipelineInterceptor<AuthenticationProcedureChallenge, ApplicationCall>
