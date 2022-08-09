package dev.forst.ktor.csp

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin

/**
 * Configuration for [ContentSecurityPolicy].
 *
 * By default it uses strict policy "default-src 'none'".
 */
class ContentSecurityPolicyConfiguration {
    /**
     * Final value that is injected to the request as a value of "Content-Security-Policy: [cspHeader]".
     */
    var cspHeader: String = "default-src 'none'"

    /**
     * Selector when not to include Content-Security-Policy header.
     */
    var skipWhen: (ApplicationCall) -> Boolean = { false }

    /**
     * Selector when not to include Content-Security-Policy header.
     */
    fun skipWhen(requestSelector: (ApplicationCall) -> Boolean) {
        skipWhen = requestSelector
    }

    /**
     * Adds policies as pairs for example.
     *
     * `"connect-src" to "'self'"
     */
    fun policy(vararg policies: Pair<String, String?>) = policy(policies.toMap())

    /**
     * Builder for policies.
     */
    fun policy(policyBuilder: () -> Map<String, String?>) = policy(policyBuilder())

    /**
     * Adds policies as map.
     */
    fun policy(policies: Map<String, String?>) {
        cspHeader = policies
            .map { (key, value) -> if (value != null) "$key $value" else key }
            .joinToString(";")
    }
}

/**
 * Plugin that injects "Content-Security-Policy" header to the request.
 */
val ContentSecurityPolicy = createRouteScopedPlugin(
    name = "ContentSecurityPolicy",
    createConfiguration = ::ContentSecurityPolicyConfiguration
) {
    val headerValue = pluginConfig.cspHeader
    val skip = pluginConfig.skipWhen

    onCallRespond { call, _ ->
        if (!skip(call)) {
            call.response.headers.append("Content-Security-Policy", headerValue)
        }
    }
}
