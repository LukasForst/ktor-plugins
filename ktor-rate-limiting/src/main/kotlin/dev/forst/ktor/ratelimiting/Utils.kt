package dev.forst.ktor.ratelimiting

/**
 * Find first element that was transformed with [transform] and its result is not null.
 *
 * If first is found, no other entry is mapped with [transform].
 */
internal inline fun <K, V, T> Map<out K, V>.firstMappingNotNullOrNull(transform: (Map.Entry<K, V>) -> T?): T? {
    for (element in this) {
        val transformResult = transform(element)
        if (transformResult != null) {
            return transformResult
        }
    }
    return null
}
