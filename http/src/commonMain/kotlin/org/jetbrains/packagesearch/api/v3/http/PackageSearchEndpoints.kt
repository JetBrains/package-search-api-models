package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.Url

public interface PackageSearchEndpoints {
    public val knownRepositories: Url
    public val packageInfoByIds: Url
    public val packageInfoByIdHashes: Url
    public val searchPackages: Url
    public val refreshPackagesInfo: Url
    public val startScroll: Url
    public val nextScroll: Url
    public val searchProjects: Url
    public val health: Url

    public companion object {
        public val DEFAULT: PackageSearchEndpoints =
            PackageSearchDefaultEndpoints(
                host = "package-search.services.jetbrains.com",
            )

        public val DEV: PackageSearchDefaultEndpoints =
            PackageSearchDefaultEndpoints(
                host = "api.dev.package-search.services.jetbrains.com",
            )

        public val PROD: PackageSearchDefaultEndpoints =
            PackageSearchDefaultEndpoints(
                host = "api.prod.package-search.services.jetbrains.com",
            )
    }
}
