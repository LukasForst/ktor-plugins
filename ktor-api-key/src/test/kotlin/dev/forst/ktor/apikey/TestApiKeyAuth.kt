package dev.forst.ktor.apikey

import io.ktor.application.call
import io.ktor.auth.Principal
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class TestApiKeyAuth {

    private data class ApiKeyPrincipal(val key: String) : Principal

    private val defaultHeader = "X-Api-Key"

    @Test
    fun `test apikey auth does not influence open routes`() {
        val apiKey = UUID.randomUUID().toString()

        val module = buildApplicationModule {
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }

        withTestApplication(module) {
            handleRequest(HttpMethod.Get, Routes.open).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, Routes.open) {
                addHeader(defaultHeader, apiKey)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleRequest(HttpMethod.Get, Routes.open) {
                addHeader(defaultHeader, "$apiKey-wrong")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

    }

    @Test
    fun `test reasonable defaults work`() {
        val apiKey = UUID.randomUUID().toString()

        val module = buildApplicationModule {
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }

        withTestApplication(module) {
            handleRequest(HttpMethod.Get, Routes.authenticated) {
                addHeader(defaultHeader, apiKey)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val principal = receive<ApiKeyPrincipal>()
                assertEquals(principal, ApiKeyPrincipal(apiKey))
            }

            handleRequest(HttpMethod.Get, Routes.authenticated) {
                addHeader(defaultHeader, "$apiKey-wrong")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }


    @Test
    fun `test auth should accept valid api key`() {
        // use different from default code to verify that it actually works
        val errorStatus = HttpStatusCode.Conflict
        val header = "hello"
        val apiKey = "world"

        val module = buildApplicationModule {
            headerName = header
            challenge { call.respond(errorStatus) }
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }

        withTestApplication(module) {
            handleRequest(HttpMethod.Get, Routes.authenticated) {
                addHeader(header, apiKey)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val principal = receive<ApiKeyPrincipal>()
                assertEquals(principal, ApiKeyPrincipal(apiKey))
            }
        }
    }

    @Test
    fun `test auth should accept reject invalid api key`() {
        val errorStatus = HttpStatusCode.Conflict
        val header = "hello"
        val apiKey = "world"

        val module = buildApplicationModule {
            headerName = header
            challenge { call.respond(errorStatus) }
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }

        withTestApplication(module) {
            handleRequest(HttpMethod.Get, Routes.authenticated) {
                addHeader(header, apiKey)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val principal = receive<ApiKeyPrincipal>()
                assertEquals(principal, ApiKeyPrincipal(apiKey))
            }

            handleRequest(HttpMethod.Get, Routes.authenticated) {
                addHeader(header, "$apiKey-wrong")
            }.apply {
                assertEquals(errorStatus, response.status())
            }
        }
    }
}
