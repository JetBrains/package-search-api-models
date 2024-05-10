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
    private fun buildUrl(vararg path: String) =
        buildUrl {
            protocol = this@PackageSearchDefaultEndpoints.protocol
            host = this@PackageSearchDefaultEndpoints.host
            pathSegments = pathSegmentsPrefix + path
            port = this@PackageSearchDefaultEndpoints.port
        }

    override val knownRepositories: Url
        get() = buildUrl(PackageSearchEndpointPaths.knownRepositories)
    override val packageInfoByIds: Url
        get() = buildUrl(PackageSearchEndpointPaths.packageInfoByIds)
    override val packageInfoByIdHashes: Url
        get() = buildUrl(PackageSearchEndpointPaths.packageInfoByIdHashes)
    override val searchPackages: Url
        get() = buildUrl(PackageSearchEndpointPaths.searchPackages)
    override val startScroll: Url
        get() = buildUrl(PackageSearchEndpointPaths.searchPackages, "scroll", "start")
    override val nextScroll: Url
        get() = buildUrl(PackageSearchEndpointPaths.searchPackages, "scroll", "next")
    override val searchProjects: Url
        get() = buildUrl(PackageSearchEndpointPaths.searchProjects)
    override val health: Url
        get() = buildUrl(PackageSearchEndpointPaths.health)
    override val refreshPackagesInfo: Url
        get() = buildUrl(PackageSearchEndpointPaths.refreshPackagesInfo)
}

private fun buildUrl(action: URLBuilder.() -> Unit): Url = URLBuilder().apply(action).build()
