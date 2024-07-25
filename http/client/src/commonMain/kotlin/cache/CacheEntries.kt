package cache

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours


internal val DEFAULT_EXPIRATION_TIME: Duration = 12.hours

@Serializable
internal data class CacheEntry<K, V>(
    val key: K,
    val value: V,
    val createdAt: Instant = Clock.System.now()
) {

    internal fun isExpired(expirationTime: Duration = DEFAULT_EXPIRATION_TIME): Boolean =
        createdAt + expirationTime < Clock.System.now()


}