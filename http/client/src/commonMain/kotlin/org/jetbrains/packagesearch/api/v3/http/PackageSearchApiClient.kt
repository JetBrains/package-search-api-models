package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.util.AttributeKey
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiProject
import org.jetbrains.packagesearch.api.v3.ApiRepository

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
                exponentialDelay()
                retryIfNot { _, httpResponse ->
                    // should NOT retry on server timeouts or 5xx
                    httpResponse.status.value in 500..599 || httpResponse.status == HttpStatusCode.RequestTimeout
                }
            }
            install(HttpTimeout) {
                requestTimeout = 30.seconds
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
            val request = kotlin.runCatching {
                defaultRawRequest(
                    method = HttpMethod.Post,
                    url = endpoints.packageInfoByIdHashes,
                    body = body,
                    cache = false
                )
            }
            val isOnline = request
                .map { it.status.isSuccess() }
                .getOrDefault(false)
            emit(isOnline)
            delay(pollingInterval)
        }
    }

}


