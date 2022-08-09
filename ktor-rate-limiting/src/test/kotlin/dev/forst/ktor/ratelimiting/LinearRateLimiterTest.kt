package dev.forst.ktor.ratelimiting

import io.mockk.every
import io.mockk.mockk
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class LinearRateLimiterTest {

    fun interface NowProvider {
        fun now(): Instant
    }

    private fun timeProvider(mockNow: Instant) = mockk<NowProvider> {
        every { now() } returns mockNow
    }

    @Test
    fun `test hit the request rate, then reset and then hit again`() {
        val limitId = UUID.randomUUID()

        val limit = 10L
        val window = Duration.ofMinutes(10)
        val now = Instant.now()
        val timeProvider = timeProvider(now)
        val diffSeconds = window.seconds

        val hostName = "hostUnderTheTest"

        val instance = LinearRateLimiter(
            limitersSettings = mapOf(limitId to Pair(limit, window)),
            nowProvider = { timeProvider.now() },
        )

        val workers = 10
        val repetitionsPerWorker = 50
        // just to check the parameters, that we can really get denied result
        assertTrue {
            @Suppress("KotlinConstantConditions") // because we sometimes change when testing
            limit < (workers + repetitionsPerWorker)
        }
        instance.stressTest(
            limitId = limitId,
            workers = workers,
            repetitionsPerWorker = repetitionsPerWorker,
            hostName = hostName,
            limit = limit,
            diffSeconds = diffSeconds
        )
        // verify that the host is still locked out
        assertEquals(diffSeconds, instance.processRequest(limitId, hostName))
        // trigger the reset
        val newNow = now.plus(window.plusMinutes(1))
        every { timeProvider.now() } returns newNow
        // verify that the host is now free to run the requests again
        instance.stressTest(
            limitId = limitId,
            workers = workers,
            repetitionsPerWorker = repetitionsPerWorker,
            hostName = hostName,
            limit = limit,
            diffSeconds = diffSeconds
        )
        // verify that the host is now locked out again
        assertEquals(diffSeconds, instance.processRequest(limitId, hostName))
    }

    @Suppress("LongParameterList") // this is a test, we're fine
    private fun LinearRateLimiter.stressTest(
        limitId: UUID,
        workers: Int,
        repetitionsPerWorker: Int,
        hostName: String,
        limit: Long,
        diffSeconds: Long
    ) {
        val limiter = this
        val counter = AtomicInteger(0)
        // execute test that will lock our host down
        runBlocking {
            repeat(workers / 2) {
                // measured host
                launch {
                    repeat(repetitionsPerWorker) {
                        val nextTime = counter.incrementAndGet()
                        val result = limiter.processRequest(limitId, hostName)
                        if (nextTime > limit) {
                            assertEquals(diffSeconds, result)
                        } else {
                            assertNull(result)
                        }
                    }
                }
                // random hosts must have an access
                launch {
                    repeat(repetitionsPerWorker) {
                        assertNull(limiter.processRequest(limitId, UUID.randomUUID().toString()))
                    }
                }
            }
        }
    }
}
