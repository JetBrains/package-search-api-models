package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodedPath

public class PackageSearchDefaultEndpoints(
    public val protocol: URLProtocol,
    public val host: String,
    public val port: Int
) : PackageSearchEndpoints {

    private fun buildPkgsUrl(path: String) = buildUrl {
        protocol = this@PackageSearchDefaultEndpoints.protocol
        host = this@PackageSearchDefaultEndpoints.host
        encodedPath = "/api/v3/$path"
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
    override val getScmsByUrl: Url
        get() = buildPkgsUrl("scms-by-url")
    override val mavenPackageInfoByFileHash: Url
        get() = buildPkgsUrl("maven-package-info-by-file-hash")
}