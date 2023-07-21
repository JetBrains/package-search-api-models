package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.protobuf.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiRepository
import org.jetbrains.packagesearch.api.v3.MavenHashLookupRequest
import org.jetbrains.packagesearch.api.v3.MavenHashLookupResponse
import org.jetbrains.packagesearch.api.v3.search.SearchParametersBuilder
import org.jetbrains.packagesearch.api.v3.search.buildSearchParameters

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

public class PackageSearchApiClient(
    public val endpoints: PackageSearchEndpoints,
    private val httpClient: HttpClient = defaultHttpClient()
) {

    public companion object {

        public fun HttpClientConfig<*>.defaultHttpClientConfig(protobuf: Boolean = true) {
            install(ContentNegotiation) {
                if (protobuf) protobuf()
                json()
            }
            install(ContentEncoding) {
                gzip()
            }
            install(HttpRequestRetry) {
                maxRetries = 5
                constantDelay(
                    delay = 500.milliseconds,
                    randomization = 100.milliseconds,
                    respectRetryAfterHeader = false
                )
            }
            install(HttpTimeout) {
                requestTimeout = 1.minutes
            }
        }

        public fun defaultHttpClient(additionalConfig: HttpClientConfig<*>.() -> Unit = {}): HttpClient =
            HttpClient {
                defaultHttpClientConfig()
                additionalConfig()
            }
    }

    private suspend inline fun <reified T, reified R> defaultRequest(url: Url, body: T) =
        httpClient.get(url) {
            setBody(body)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }.body<R>()

    private suspend inline fun <reified R> defaultRequest(url: Url) =
        httpClient.get(url) {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }.body<R>()

    public suspend fun getKnownRepositories(): List<ApiRepository> =
        defaultRequest(endpoints.knownRepositories)

    public suspend fun getPackageInfoByIds(ids: Set<String>): List<ApiPackage> =
        defaultRequest<_, GetPackageInfoResponse>(endpoints.packageInfoByIds, GetPackageInfoRequest(ids)).packages

    public suspend fun getPackageInfoByIdHashes(ids: Set<String>): List<ApiPackage> =
        defaultRequest<_, GetPackageInfoResponse>(endpoints.packageInfoByIdHashes, GetPackageInfoRequest(ids)).packages

    public suspend fun searchPackages(request: SearchPackagesRequest): List<ApiPackage> =
        defaultRequest<_, SearchPackagesResponse>(endpoints.searchPackages, request).packages

    public suspend fun getMavenPackageInfoByFileHash(request: MavenHashLookupRequest): MavenHashLookupResponse =
        defaultRequest(endpoints.mavenPackageInfoByFileHash, request)

    public suspend fun getScmByUrl(request: GetScmByUrlRequest): String? =
        defaultRequest(endpoints.getScmsByUrl, request)
}

public suspend fun PackageSearchApiClient.searchPackages(builder: SearchParametersBuilder.() -> Unit): List<ApiPackage> =
    searchPackages(buildSearchParameters(builder))