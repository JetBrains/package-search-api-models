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
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiProject
import org.jetbrains.packagesearch.api.v3.ApiRepository
import org.jetbrains.packagesearch.api.v3.search.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

public interface PackageSearchApi {
    public suspend fun getKnownRepositories(): List<ApiRepository>
    public suspend fun getPackageInfoByIds(ids: Set<String>): Map<String, ApiPackage>
    public suspend fun getPackageInfoByIdHashes(ids: Set<String>): Map<String, ApiPackage>
    public suspend fun searchPackages(request: SearchPackagesRequest): List<ApiPackage>
    public suspend fun startScroll(request: SearchPackagesStartScrollRequest): SearchPackagesScrollResponse
    public suspend fun nextScroll(request: SearchPackagesNextScrollRequest): SearchPackagesScrollResponse
    public suspend fun searchProjects(request: SearchProjectRequest): List<ApiProject>
    public fun isOnlineFlow(pollingInterval: Duration = 1.seconds): Flow<Boolean>
}

public class PackageSearchApiClient(
    public val endpoints: PackageSearchEndpoints,
    private val httpClient: HttpClient = defaultHttpClient(),
) : PackageSearchApi {

    @Suppress("UNUSED_PARAMETER")
    @Deprecated(
        message = "Use new constructor",
        replaceWith = ReplaceWith("PackageSearchApiClient(endpoints, httpClient)")
    )
    public constructor(
        endpoints: PackageSearchEndpoints,
        httpClient: HttpClient = defaultHttpClient(),
        scope: CoroutineScope,
        pollingInterval: Duration = 1.seconds,
    ) : this(endpoints, httpClient)

    public companion object {

        private fun HttpClientConfig<*>.defaultEngineConfig(protobuf: Boolean = true) {
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
        }

        public fun defaultHttpClient(
            protobuf: Boolean = true,
            additionalConfig: HttpClientConfig<*>.() -> Unit = {},
        ): HttpClient = HttpClient {
            defaultEngineConfig(protobuf)
            additionalConfig()
        }

        public fun <T : HttpClientEngineConfig> defaultHttpClient(
            engine: HttpClientEngineFactory<T>,
            protobuf: Boolean = true,
            additionalConfig: HttpClientConfig<T>.() -> Unit = {},
        ): HttpClient = HttpClient(engine) {
            defaultEngineConfig(protobuf)
            additionalConfig()
        }

        public fun defaultHttpClient(
            engine: HttpClientEngine,
            protobuf: Boolean = true,
            additionalConfig: HttpClientConfig<*>.() -> Unit = {},
        ): HttpClient = HttpClient(engine) {
            defaultEngineConfig(protobuf)
            additionalConfig()
        }

    }

    public object Attributes {
        public val Cache: AttributeKey<Boolean> = AttributeKey("cache")
    }

    private suspend inline fun <reified T> defaultRawRequest(
        method: HttpMethod,
        url: Url,
        body: T,
        cache: Boolean = true,
    ) = httpClient.request(url) {
        this@request.method = method
        setBody(body)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        attributes.put(Attributes.Cache, cache)
    }

    private suspend inline fun <reified T, reified R> defaultRequest(method: HttpMethod, url: Url, body: T) =
        defaultRawRequest<T>(method, url, body).body<R>()

    private suspend inline fun <reified R> defaultRequest(method: HttpMethod, url: Url) =
        httpClient.request(url) {
            this@request.method = method
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }.body<R>()

    override suspend fun getKnownRepositories(): List<ApiRepository> =
        defaultRequest(HttpMethod.Get, endpoints.knownRepositories)

    override suspend fun getPackageInfoByIds(ids: Set<String>): Map<String, ApiPackage> =
        defaultRequest<_, List<ApiPackage>>(HttpMethod.Post, endpoints.packageInfoByIds, GetPackageInfoRequest(ids))
            .associateBy { it.id }

    override suspend fun getPackageInfoByIdHashes(ids: Set<String>): Map<String, ApiPackage> =
        defaultRequest<_, List<ApiPackage>>(
            HttpMethod.Post,
            endpoints.packageInfoByIdHashes,
            GetPackageInfoRequest(ids)
        )
            .associateBy { it.id }

    override suspend fun searchPackages(request: SearchPackagesRequest): List<ApiPackage> =
        defaultRequest<_, List<ApiPackage>>(HttpMethod.Post, endpoints.searchPackages, request)

    override suspend fun startScroll(request: SearchPackagesStartScrollRequest): SearchPackagesScrollResponse =
        defaultRequest<_, SearchPackagesScrollResponse>(HttpMethod.Post, endpoints.startScroll, request)

    override suspend fun nextScroll(request: SearchPackagesNextScrollRequest): SearchPackagesScrollResponse =
        defaultRequest<_, SearchPackagesScrollResponse>(HttpMethod.Post, endpoints.nextScroll, request)

    override suspend fun searchProjects(request: SearchProjectRequest): List<ApiProject> =
        defaultRequest<_, List<ApiProject>>(HttpMethod.Post, endpoints.searchPackages, request)

    override fun isOnlineFlow(pollingInterval: Duration): Flow<Boolean> = flow {
        while (true) {
            val body = GetPackageInfoRequest(setOf(ApiPackage.hashPackageId("maven:io.ktor:ktor-client-core")))
            val request = defaultRawRequest(
                method = HttpMethod.Post,
                url = endpoints.packageInfoByIdHashes,
                body = body,
                cache = false
            )
            val isOnline = request.status.isSuccess()
            emit(isOnline)
            delay(pollingInterval)
        }
    }

}

public suspend fun PackageSearchApiClient.searchPackages(builder: SearchParametersBuilder.() -> Unit): List<ApiPackage> =
    searchPackages(buildSearchParameters(builder))

public suspend fun PackageSearchApiClient.startScroll(builder: StartScrollParametersBuilder.() -> Unit): SearchPackagesScrollResponse =
    startScroll(buildStartScrollParameters(builder))

public suspend fun PackageSearchApiClient.nextScroll(builder: NextScrollParametersBuilder.() -> Unit): SearchPackagesScrollResponse =
    nextScroll(buildNextScrollParameters(builder))

