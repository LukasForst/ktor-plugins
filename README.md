# Ktor Plugins

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/LukasForst/ktor-plugins?style=flat-square)

Collection of useful Ktor plugins. All plugins are hosted on Maven central and can be added to your project as easy as:

```kotlin
implementation("dev.forst", "ktor-<plugin>", "<latest version>")
```

* [ktor-api-key](ktor-api-key)
    * simple authentication provider for Ktor that verifies presence of the API key in the header
* [ktor-content-security-policy](ktor-content-security-policy)
    * plugin that allows setting [Content-Security-Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
      headers
* [ktor-openapi-generator](https://github.com/LukasForst/ktor-openapi-generator/)
    * generates OpenAPI definitions from your server with support for Ktor `>= 2.0.0`
    * developed in [separate repository](https://github.com/LukasForst/ktor-openapi-generator/) because it is a fork of
      existing project
* [ktor-rate-limiting](ktor-rate-limiting)
    * plugin that enables rate limiting in Ktor

## Ktor API Key Authentication Provider

Simple authentication provider for Ktor that verifies presence of the API key in the header. Useful if you want to
use `X-Api-Key` or similar approaches for request authentication.

```kotlin
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
```

## Ktor Content Security Policy

Plugin that allows setting [Content-Security-Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP) headers.

```kotlin
/**
 * Minimal Ktor application using Content Security Policy.
 */
fun Application.minimalExample() {
    install(ContentSecurityPolicy) {
        skipWhen { call ->
            call.request.path().startsWith("/some-ignored-route")
        }
        policy(
            "default-src" to "'none'"
        )
    }
}
```

## Ktor Rate Limiting

A simple library that enables Rate Limiting in Ktor.

```kotlin
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
            call.request.origin.host
        }
        // and exclude path which ends with "excluded"
        excludeRequestWhen {
            call.request.path().endsWith("excluded")
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
```