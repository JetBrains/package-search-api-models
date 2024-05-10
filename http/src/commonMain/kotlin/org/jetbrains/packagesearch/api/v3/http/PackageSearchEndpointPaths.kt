package org.jetbrains.packagesearch.api.v3.http

public data object PackageSearchEndpointPaths {
    public const val knownRepositories: String = "known-repositories"
    public const val packageInfoByIds: String = "package-info-by-ids"
    public const val packageInfoByIdHashes: String = "package-info-by-id-hashes"
    public const val searchPackages: String = "search-packages"
    public const val startScroll: String = "search-packages"
    public const val nextScroll: String = "search-packages"
    public const val searchProjects: String = "search-projects"
    public const val health: String = "health"
    public const val refreshPackagesInfo: String = "refresh-packages-info"
}