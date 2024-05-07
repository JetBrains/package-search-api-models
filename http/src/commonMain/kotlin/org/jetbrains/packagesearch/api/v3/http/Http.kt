package org.jetbrains.packagesearch.api.v3.http

import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.search.PackagesType

@Serializable
public data class GetPackageInfoRequest(
    public val ids: Set<String> = emptySet(),
)

@Serializable
public data class SearchProjectRequest(
    public val query: String,
)

// Provide only 50 search results per batch by default
public const val DEFAULT_BATCH_SIZE: Int = 50

@Serializable
public data class SearchPackagesRequest(
    public val packagesType: List<PackagesType> = emptyList(),
    public val searchQuery: String,
)

@Serializable
public data class RefreshPackagesInfoRequest(
    public val packages: List<CacheRequest>,
) {
    @Serializable
    public data class CacheRequest(
        public val packageId: String,
        public val latestKnownVersion: String,
    )
}

// Keep the search context alive for 5 minutes (per batch)
public const val DEFAULT_SCROLL_DURATION: String = "5m"

@Serializable
public data class SearchPackagesStartScrollRequest(
    public val packagesType: List<PackagesType> = emptyList(),
    public val searchQuery: String = "",
    public val batchSize: Int = DEFAULT_BATCH_SIZE,
    public val duration: String = DEFAULT_SCROLL_DURATION,
)

@Serializable
public data class SearchPackagesNextScrollRequest(
    public val scrollId: String,
    public val duration: String = DEFAULT_SCROLL_DURATION,
)

@Serializable
public data class SearchPackagesScrollResponse(
    val scrollId: String?,
    val data: List<ApiPackage>,
)

@Serializable
public data class SearchProjectsResponse(
    public val query: Query,
    public val packages: List<ApiPackage> = emptyList(),
) {
    @Serializable
    public data class Query(
        public val query: String,
        public val onlyStable: Boolean,
    )
}

@Serializable
public data class GetScmByUrlRequest(
    public val urls: List<String> = emptyList(),
)

@Serializable
public data class ProcessScmRequest(
    public val url: String,
    public val pkgIdHash: String,
)
