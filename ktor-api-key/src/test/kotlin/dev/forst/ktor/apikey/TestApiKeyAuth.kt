package dev.forst.ktor.apikey

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.auth.Principal
import io.ktor.server.response.respond
import io.ktor.server.testing.testApplication
import java.util.*
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class TestApiKeyAuth {

    private data class ApiKeyPrincipal(val key: String) : Principal

    private val defaultHeader = "X-Api-Key"

    @Test
    fun `test apikey auth does not influence open routes`() {
        val apiKey = UUID.randomUUID().toString()

        val module = buildApplicationModule {
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }

        testApplication {
            application(module)
            val client = createClient { install(ContentNegotiation) { jackson() } }

            var response = client.get(Routes.open)
            assertEquals(HttpStatusCode.OK, response.status)

            response = client.get(Routes.open) {
                header(defaultHeader, apiKey)
            }
            assertEquals(HttpStatusCode.OK, response.status)

            response = client.get(Routes.open) {
                header(defaultHeader, "$apiKey-wrong")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `test reasonable defaults work`() {
        val apiKey = UUID.randomUUID().toString()

        val module = buildApplicationModule {
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }

        testApplication {
            application(module)
            val client = createClient { install(ContentNegotiation) { jackson() } }

            // correct header
            val response = client.get(Routes.authenticated) {
                header(defaultHeader, apiKey)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val principal = response.body<ApiKeyPrincipal>()
            assertEquals(principal, ApiKeyPrincipal(apiKey))

            // incorrect header
            val unauthorizedResponse = client.get(Routes.authenticated) {
                header(defaultHeader, "$apiKey-wrong")
            }
            assertEquals(HttpStatusCode.Unauthorized, unauthorizedResponse.status)
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
            challenge { call -> call.respond(errorStatus) }
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }
        testApplication {
            application(module)
            val client = createClient { install(ContentNegotiation) { jackson() } }

            val response = client.get(Routes.authenticated) {
                header(header, apiKey)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val principal = response.body<ApiKeyPrincipal>()
            assertEquals(principal, ApiKeyPrincipal(apiKey))
        }
    }

    @Test
    fun `test auth should accept reject invalid api key`() {
        val errorStatus = HttpStatusCode.Conflict
        val header = "hello"
        val apiKey = "world"

        val module = buildApplicationModule {
            headerName = header
            challenge { call -> call.respond(errorStatus) }
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }
        testApplication {
            application(module)
            val client = createClient { install(ContentNegotiation) { jackson() } }

            // correct header
            val response = client.get(Routes.authenticated) {
                header(header, apiKey)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val principal = response.body<ApiKeyPrincipal>()
            assertEquals(principal, ApiKeyPrincipal(apiKey))

            // incorrect header
            val unauthorizedResponse = client.get(Routes.authenticated) {
                header(header, "$apiKey-wrong")
            }
            assertEquals(errorStatus, unauthorizedResponse.status)
        }
    }
}
