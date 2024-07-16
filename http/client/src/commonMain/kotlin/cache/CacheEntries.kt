package cache

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
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
    val _id: Long? = null,
    val values: List<ApiRepository>,
) {
    val expires: Instant = Clock.System.now().plus(SHORT_EXPIRATION_TIME)
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}


@Serializable
internal data class ApiPackageCacheEntry(
    val _id: Long? = null,
    val apiPackage: ApiPackage
) {
    val id: String = apiPackage.id

    @SerialName("_id")
    val idHash: String = apiPackage.idHash

    val expires: Instant = Clock.System.now().plus(DEFAULT_EXPIRATION_TIME)
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}

@Serializable
internal data class SearchPackageRequestCacheEntry(
    val _id: Long? = null,
    val request: SearchPackagesRequest,
    val packages: List<ApiPackage>
) {
    val searchQuery: String = request.searchQuery

    val expires: Instant = Clock.System.now().plus(DEFAULT_EXPIRATION_TIME)
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}

@Serializable
internal data class SearchPackageScrollCacheEntry(
    val _id: Long? = null,
    val scrollId: String,
    val packages: List<ApiPackage>
) {
    val expires: Instant = Clock.System.now().plus(DEFAULT_EXPIRATION_TIME)
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}

@Serializable
internal data class ApiProjectsCacheEntry(
    val _id: Long? = null,
    val queryString: String,
    val values: List<ApiProject>
) {
    val expires: Instant = Clock.System.now().plus(DEFAULT_EXPIRATION_TIME)
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}