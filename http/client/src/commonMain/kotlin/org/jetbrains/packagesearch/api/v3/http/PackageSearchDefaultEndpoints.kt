package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.URLProtocol
import io.ktor.http.Url


public class PackageSearchDefaultEndpoints(
    public val protocol: URLProtocol,
    public val host: String,
    public val segments: List<String> = listOf("api", "v3"),
    public val port: Int = protocol.defaultPort
) : PackageSearchEndpoints {

    public companion object {
        public val DEV: PackageSearchDefaultEndpoints = PackageSearchDefaultEndpoints(
            protocol = URLProtocol.HTTPS,
            host = "api.dev.package-search.services.jetbrains.com",
            segments = emptyList(),
        )
    }

    private fun buildPkgsUrl(path: String) = buildUrl {
        protocol = this@PackageSearchDefaultEndpoints.protocol
        host = this@PackageSearchDefaultEndpoints.host
        pathSegments = segments + path
        port = this@PackageSearchDefaultEndpoints.port
    }

    override val knownRepositories: Url
        get() = buildPkgsUrl("known-repositories")
    override val packageInfoByIds: Url
        get() = buildPkgsUrl("package-info-by-ids")
    override val packageInfoByIdHashes: Url
        get() = buildPkgsUrl("package-info-by-id-hashes")
    override val searchPackages: Url
        get() = buildPkgsUrl("search-packages")
    override val searchProjects: Url
        get() = buildPkgsUrl("search-projects")
}