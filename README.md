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

## Ktor OpenAPI Generator & Swagger UI

Ktor OpenAPI plugin (with support for Ktor `2.x.y`) that generates OpenAPI from your routes definition and allows you to
host Swagger UI. Hosted on different
repository: [LukasForst/ktor-openapi-generator](https://github.com/LukasForst/ktor-openapi-generator/) as it is a fork
of popular [papsign/Ktor-OpenAPI-Generator](https://github.com/papsign/Ktor-OpenAPI-Generator) with some refactoring and
support for Ktor `2.x.y`.

It supports most of the stuff from Ktor including JWT and Session auth.

```kotlin
/**
 * Minimal example of OpenAPI plugin for Ktor.
 */
fun Application.minimalExample() {
    // install OpenAPI plugin
    install(OpenAPIGen) {
        // this automatically servers Swagger UI on /swagger-ui
        serveSwaggerUi = true
        info {
            title = "Minimal Example API"
        }
    }
    // install JSON support
    install(ContentNegotiation) {
        jackson()
    }
    // add basic routes for openapi.json and redirect to UI
    routing {
        // serve openapi.json
        get("/openapi.json") {
            call.respond(this@routing.application.openAPIGen.api.serialize())
        }
        // and do redirect to make it easier to remember
        get("/swagger-ui") {
            call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
        }
    }
    // and now example routing
    apiRouting {
        route("/example/{name}") {
            // SomeParams are parameters (query or path), SomeResponse is what the backend returns and SomeRequest
            // is what was passed in the body of the request
            post<SomeParams, SomeResponse, SomeRequest> { params, someRequest ->
                respond(SomeResponse(bar = "Hello ${params.name}! From body: ${someRequest.foo}."))
            }
        }
    }
}

data class SomeParams(@PathParam("who to say hello") val name: String)
data class SomeRequest(val foo: String)
data class SomeResponse(val bar: String)
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