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
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiProject
import org.jetbrains.packagesearch.api.v3.ApiRepository
import org.jetbrains.packagesearch.api.v3.search.SearchParametersBuilder
import org.jetbrains.packagesearch.api.v3.search.buildSearchParameters

public expect val DefaultEngine: HttpClientEngineFactory<HttpClientEngineConfig>

public interface PackageSearchApi {
    public suspend fun getKnownRepositories(): List<ApiRepository>
    public suspend fun getPackageInfoByIds(ids: Set<String>): Map<String, ApiPackage>
    public suspend fun getPackageInfoByIdHashes(ids: Set<String>): Map<String, ApiPackage>
    public suspend fun searchPackages(request: SearchPackagesRequest): List<ApiPackage>
    public suspend fun searchProjects(request: SearchProjectRequest): List<ApiProject>
}

public class PackageSearchApiClient(
    public val endpoints: PackageSearchEndpoints,
    private val httpClient: HttpClient = defaultHttpClient()
) : PackageSearchApi {

    public companion object {

        public fun defaultHttpClient(protobuf: Boolean = true, additionalConfig: HttpClientConfig<*>.() -> Unit = {}): HttpClient =
            HttpClient(DefaultEngine) {
                install(ContentNegotiation) {
                    if (protobuf) protobuf(ProtoBuf { encodeDefaults = false })
                    json()
                }
                install(ContentEncoding) {
                    gzip()
                }
                install(HttpRequestRetry) {
                    maxRetries = 3
                    constantDelay(
                        delay = 500.milliseconds,
                        randomization = 100.milliseconds,
                        respectRetryAfterHeader = false
                    )
                }
                install(HttpTimeout) {
                    requestTimeout = 10.seconds
                }
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

    override suspend fun getKnownRepositories(): List<ApiRepository> =
        defaultRequest(endpoints.knownRepositories)

    override suspend fun getPackageInfoByIds(ids: Set<String>): Map<String, ApiPackage> =
        defaultRequest<_, List<ApiPackage>>(endpoints.packageInfoByIds, GetPackageInfoRequest(ids))
            .associateBy { it.id }

    override suspend fun getPackageInfoByIdHashes(ids: Set<String>): Map<String, ApiPackage> =
        defaultRequest<_, List<ApiPackage>>(endpoints.packageInfoByIdHashes, GetPackageInfoRequest(ids))
            .associateBy { it.id }

    override suspend fun searchPackages(request: SearchPackagesRequest): List<ApiPackage> =
        defaultRequest<_, List<ApiPackage>>(endpoints.searchPackages, request)

    override suspend fun searchProjects(request: SearchProjectRequest): List<ApiProject> =
        defaultRequest<_, List<ApiProject>>(endpoints.searchPackages, request)

}

public suspend fun PackageSearchApiClient.searchPackages(builder: SearchParametersBuilder.() -> Unit): List<ApiPackage> =
    searchPackages(buildSearchParameters(builder))