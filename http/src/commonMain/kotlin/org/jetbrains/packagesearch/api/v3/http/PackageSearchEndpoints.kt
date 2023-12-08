package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.*

public interface PackageSearchEndpoints {
    public val knownRepositories: Url
    public val packageInfoByIds: Url
    public val packageInfoByIdHashes: Url
    public val searchPackages: Url
    public val startScroll: Url
    public val nextScroll: Url
    public val searchProjects: Url

    public companion object {
        public val DEV: PackageSearchDefaultEndpoints = PackageSearchDefaultEndpoints(
            protocol = URLProtocol.HTTPS,
            host = "api.dev.package-search.services.jetbrains.com",
        )
    }
}
