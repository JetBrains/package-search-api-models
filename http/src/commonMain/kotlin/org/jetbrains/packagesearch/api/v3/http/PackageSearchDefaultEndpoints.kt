package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.*

public class PackageSearchDefaultEndpoints(
    public val protocol: URLProtocol,
    public val host: String,
    public val pathSegmentsPrefix: List<String> = emptyList(),
    public val port: Int = protocol.defaultPort
) : PackageSearchEndpoints {

    private fun buildPkgsUrl(path: String) = buildUrl {
        protocol = this@PackageSearchDefaultEndpoints.protocol
        host = this@PackageSearchDefaultEndpoints.host
        pathSegments = pathSegmentsPrefix + path
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

private fun buildUrl(action: URLBuilder.() -> Unit): Url = URLBuilder().apply(action).build()
