package dev.forst.ktor.ratelimiting

import java.time.Duration

/**
 * Reasonable values for the default settings of rate limiter.
 */

/**
 * Default value for [LinearRateLimiter.purgeHitSize].
 */
internal const val DEFAULT_PURGE_HIT_SIZE: Int = 100

/**
 * Default value for [LinearRateLimiter.initialSize].
 */
@Suppress("KDocUnresolvedReference") // constructor parameter
internal const val DEFAULT_INITIAL_CACHE_SIZE: Int = 64

/**
 * Default value for [LinearRateLimiter.purgeHitDuration].
 */
@Suppress("MagicNumber")
internal val DEFAULT_PURGE_HIT_DURATION: Duration = Duration.ofMinutes(10L)
