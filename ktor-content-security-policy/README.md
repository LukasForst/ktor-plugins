# Ktor Content Security Policy Plugin

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/LukasForst/ktor-plugins?style=flat-square)

Plugin that allows setting [Content-Security-Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP) headers.

## Installation

Include following in your `build.gradle.kts`:

```kotlin
implementation("dev.forst", "ktor-content-security-policy", "<latest version>")
```

## Usage

Minimal usage:

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

For details see [MinimalExampleApp.kt](src/test/kotlin/dev/forst/ktor/csp/MinimalExampleApp.kt) with this example
application and [TestMinimalExampleApp.kt](src/test/kotlin/dev/forst/ktor/csp/TestMinimalExampleApp.kt) which verifies
that this app works as expected.