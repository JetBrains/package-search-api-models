package cache

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiRepository
import org.jetbrains.packagesearch.api.v3.http.SearchPackagesRequest
import kotlin.time.Duration.Companion.hours

@Serializable
internal data class ApiRepositoryCacheEntry(
    val values: List<ApiRepository>,

) {
    val expires: Instant = Clock.System.now().plus(6.hours)
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}


@Serializable
internal data class ApiPackageCacheEntry(
    val apiPackage: ApiPackage
) {
    val id: String = apiPackage.id
    @SerialName("_id")val idHash: String = apiPackage.idHash

    val expires: Instant = Clock.System.now().plus(12.hours)
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}

@Serializable
internal data class SearchPackageRequestEntry(
    val request: SearchPackagesRequest,
    val packages: List<ApiPackage>
) {
    val searchQuery: String = request.searchQuery

//    @SerialName("_id")val search: String = request.hashCode()

    val expires: Instant = Clock.System.now().plus(12.hours)
    val isExpired: Boolean
        get() = expires < Clock.System.now()
}