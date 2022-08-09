package dev.forst.ktor.apikey

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.server.testing.TestApplicationCall
import kotlin.test.assertNotNull

inline fun <reified T> TestApplicationCall.receiveOrNull(): T? {
    val content = response.content ?: return null
    return jacksonObjectMapper().readValue(content)
}

inline fun <reified T> TestApplicationCall.receive(): T =
    assertNotNull(receiveOrNull<T>(), "Received content was null!")
