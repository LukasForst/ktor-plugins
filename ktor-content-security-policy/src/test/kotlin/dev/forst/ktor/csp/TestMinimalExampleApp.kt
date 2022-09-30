package dev.forst.ktor.csp

import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestMinimalExampleApp {
    @Test
    fun `test csp header is correct`() {
        assertEquals("Content-Security-Policy", HttpHeaders.ContentSecurityPolicy)
    }

    @Test
    fun `test minimal example app works as expected`() = testApplication {
        application(Application::minimalExample)
        // this should return csp header
        var responseWithCsp = client.get("/")
        assertEquals("default-src 'self'", responseWithCsp.headers[HttpHeaders.ContentSecurityPolicy])
        responseWithCsp = client.get("/specific")
        assertEquals("default-src 'none'", responseWithCsp.headers[HttpHeaders.ContentSecurityPolicy])
        // this should not
        val skippedResponse = client.get("/ignored")
        assertNull(skippedResponse.headers[HttpHeaders.ContentSecurityPolicy])
    }
}
