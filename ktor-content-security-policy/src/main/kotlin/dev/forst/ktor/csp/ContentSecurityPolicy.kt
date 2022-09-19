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
     * Selector when not to include Content-Security-Policy header.
     */
    var skipWhen: (ApplicationCall) -> Boolean = { false }

    /**
     * Call specific policy, if it returns map, it is used as a csp policy. When returns null,
     * the CSP Header is not set.
     *
     * By default, it uses strict policy "default-src 'none'".
     */
    var policy: (call: ApplicationCall, body: Any) -> Map<String, String?>? = { _, _ -> mapOf("default-src" to "'none'") }

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
     * Adds policies as map. These are used if there are no other more specific policies set
     * for the given call.
     */
    fun policy(policies: Map<String, String?>) {
        policy { _, _ -> policies }
    }

    /**
     * Adds policies that are specific per request, when [policyBuilder] returns null, no CSP header is set.
     */
    fun policy(policyBuilder: (call: ApplicationCall, body: Any) -> Map<String, String?>?) {
        this.policy = policyBuilder
    }
}

/**
 * Plugin that injects "Content-Security-Policy" header to the request.
 */
val ContentSecurityPolicy = createRouteScopedPlugin(
    name = "ContentSecurityPolicy",
    createConfiguration = ::ContentSecurityPolicyConfiguration
) {
    val policy = pluginConfig.policy
    val skip = pluginConfig.skipWhen

    onCallRespond { call, body ->
        if (!skip(call)) {
            val header = policy(call, body)?.toCspHeader() ?: return@onCallRespond
            call.response.headers.append("Content-Security-Policy", header)
        }
    }
}

private fun Map<String, String?>.toCspHeader(): String = this
    .map { (key, value) -> if (value != null) "$key $value" else key }
    .joinToString(";")
