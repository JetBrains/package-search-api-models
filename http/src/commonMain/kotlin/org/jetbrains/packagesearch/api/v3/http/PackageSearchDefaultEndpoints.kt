package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url

public class PackageSearchDefaultEndpoints(
    public val host: String,
    public val protocol: URLProtocol = URLProtocol.HTTPS,
    public val pathSegmentsPrefix: List<String> = emptyList(),
    public val port: Int = protocol.defaultPort,
) : PackageSearchEndpoints {

    private fun buildUrl(vararg path: String) = buildUrl {
        protocol = this@PackageSearchDefaultEndpoints.protocol
        host = this@PackageSearchDefaultEndpoints.host
        pathSegments = pathSegmentsPrefix + path
        port = this@PackageSearchDefaultEndpoints.port
    }

    override val knownRepositories: Url
        get() = buildUrl("known-repositories")
    override val packageInfoByIds: Url
        get() = buildUrl("package-info-by-ids")
    override val packageInfoByIdHashes: Url
        get() = buildUrl("package-info-by-id-hashes")
    override val searchPackages: Url
        get() = buildUrl("search-packages")
    override val startScroll: Url
        get() = buildUrl("search-packages", "scroll", "start")
    override val nextScroll: Url
        get() = buildUrl("search-packages", "scroll", "next")
    override val searchProjects: Url
        get() = buildUrl("search-projects")
    override val health: Url
        get() = buildUrl("health")
    override val refreshPackagesInfo: Url
        get() = buildUrl("refresh-packages-info")
}

private fun buildUrl(action: URLBuilder.() -> Unit): Url = URLBuilder().apply(action).build()
