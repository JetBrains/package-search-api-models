package cache

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiProject
import org.jetbrains.packagesearch.api.v3.ApiRepository
import org.jetbrains.packagesearch.api.v3.http.SearchPackagesRequest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours


public val DEFAULT_EXPIRATION_TIME: Duration = 12.hours
public val SHORT_EXPIRATION_TIME: Duration = 6.hours

@Serializable
internal data class ApiRepositoryCacheEntry(
    val values: List<ApiRepository>,
    val expires: Instant = Clock.System.now().plus(SHORT_EXPIRATION_TIME)
) {
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}

internal fun List<ApiRepository>.toCacheEntry(): ApiRepositoryCacheEntry = ApiRepositoryCacheEntry(this)

@Serializable
internal data class ApiPackageCacheEntry(
    val apiPackage: ApiPackage,
    val expires: Instant = Clock.System.now().plus(DEFAULT_EXPIRATION_TIME)
) {
    val id: String = apiPackage.id

    val idHash: String = apiPackage.idHash
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}

internal fun ApiPackage.toCacheEntry(): ApiPackageCacheEntry = ApiPackageCacheEntry(this)

@Serializable
internal data class SearchPackageRequestCacheEntry(
    val request: SearchPackagesRequest,
    val packages: List<ApiPackage>,
    val expires: Instant = Clock.System.now().plus(SHORT_EXPIRATION_TIME)
) {

    val isExpired: Boolean
        get() = expires < Clock.System.now()
}

@Serializable
internal data class SearchPackageScrollCacheEntry(
    val scrollId: String,
    val packages: List<ApiPackage>,
    val expires: Instant = Clock.System.now().plus(SHORT_EXPIRATION_TIME)
) {
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}

@Serializable
internal data class ApiProjectsCacheEntry(
    val queryString: String,
    val values: List<ApiProject>,
    val expires: Instant = Clock.System.now().plus(SHORT_EXPIRATION_TIME)
) {
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}