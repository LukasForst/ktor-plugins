package dev.forst.ktor.apikey

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestMinimalExampleApp {
    @Test
    fun `test minimal example app works as expected`() {
        withTestApplication(Application::minimalExample) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            handleRequest(HttpMethod.Get, "/") {
                addHeader("X-Api-Key", "this-is-expected-key")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Key: this-is-expected-key", response.content)
            }
        }
    }
}
