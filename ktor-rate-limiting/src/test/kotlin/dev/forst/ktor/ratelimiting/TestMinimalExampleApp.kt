package dev.forst.ktor.ratelimiting

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class TestMinimalExampleApp {
    @Test
    fun `test minimal example app works as expected`() = testApplication {
        application(Application::minimalExample)
        // 10 times our request should pass
        repeat(10) {
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Hello localhost", response.bodyAsText())
        }
        // and then it should be blocked
        val blockedResponse = client.get("/")
        assertEquals(HttpStatusCode.TooManyRequests, blockedResponse.status)

        // but excluded route should be still available
        val excludedResponse = client.get("/excluded")
        assertEquals(HttpStatusCode.OK, excludedResponse.status)
        assertEquals("Hello localhost", excludedResponse.bodyAsText())
    }
}
