package dev.forst.ktor.apikey

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class TestMinimalExampleApp {
    @Test
    fun `test minimal example app works as expected`() {
        testApplication {
            application(Application::minimalExample)

            val unauthorizedResponse = client.get("/")
            assertEquals(HttpStatusCode.Unauthorized, unauthorizedResponse.status)

            val response = client.get("/") {
                header("X-Api-Key", "this-is-expected-key")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Key: this-is-expected-key", response.bodyAsText())
        }
    }
}
