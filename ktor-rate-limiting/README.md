# Ktor Rate Limiting

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/LukasForst/ktor-plugins?style=flat-square)

A simple library that enables Rate Limiting in Ktor. Originally
hosted in [LukasForst/ktor-rate-limiting](https://github.com/LukasForst/ktor-rate-limiting) repository.

## Installation

Include following in your `build.gradle.kts`:

```kotlin
implementation("dev.forst", "ktor-rate-limiting", "<latest version>")
```

Versions >= `1.2.0` have implementation for Ktor >= `2.0.0`, use `1.1.0` if you need support for older versions of Ktor.

## Usage

This is minimal implementation of the Ktor app that uses Rate Limiting:

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

For details see [MinimalExampleApp.kt](src/test/kotlin/dev/forst/ktor/ratelimiting/MinimalExampleApp.kt) with this
example application
and [TestMinimalExampleApp.kt](src/test/kotlin/dev/forst/ktor/ratelimiting/TestMinimalExampleApp.kt) which verifies that
this app works as
expected.
