package dev.forst.ktor.csp

import io.ktor.client.request.get
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

class TestMinimalExampleApp {
    @Test
    fun `test minimal example app works as expected`() = testApplication {
        application(Application::minimalExample)
        // this should return csp header
        val responseWithCsp = client.get("/")
        assertEquals("default-src 'none'", responseWithCsp.headers["Content-Security-Policy"])
        // this should not
        val skippedResponse = client.get("/ignored")
        assertNull(skippedResponse.headers["Content-Security-Policy"])
    }
}
