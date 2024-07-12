package org.jetbrains.packagesearch.api.v3.http

import cache.ApiPackageCacheEntry
import cache.ApiRepositoryCacheEntry
import cache.CacheDB
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.protobuf.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.document.database.DataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiProject
import org.jetbrains.packagesearch.api.v3.ApiRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

public class PackageSearchApiClient(
    public val endpoints: PackageSearchEndpoints,
    private val httpClient: HttpClient = defaultHttpClient(),
    dataStore: DataStore,
) {

    private val cachedb = CacheDB(dataStore)

    private suspend fun isOffline() = httpClient.isOffline()


    @Serializable
    private data class Error(val error: Inner) {
        @Serializable
        data class Inner(val message: String, val stackTrace: List<String>)
    }

    public companion object {
        private fun HttpClientConfig<*>.defaultEngineConfig(protobuf: Boolean = true) {
            install(ContentNegotiation) {
                if (protobuf) protobuf(ProtoBuf { encodeDefaults = false })
                json()
            }
            install(HttpCallValidator) {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        response.bodyAsText()
                            .runCatching { Json.decodeFromString<Error>(this) }
                            .onSuccess { throw it.toException(response) }
                    }
                }
            }
            install(ContentEncoding) {
                gzip()
            }
            install(HttpRequestRetry) {
                maxRetries = 3
                exponentialDelay()
                retryIf(3) { _, httpResponse ->
                    httpResponse.status.value in 500..599 || httpResponse.status == HttpStatusCode.RequestTimeout
                }
            }
            install(HttpTimeout) {
                requestTimeout = 30.seconds
            }
        }

        private fun Error.toException(response: HttpResponse) =
            PackageSearchApiException(
                serverMessage = error.message,
                request = response.request.toSerializable(),
                statusCode = response.status.toSerializable(),
                remoteStackTrace = error.stackTrace,
            )

        public fun defaultHttpClient(
            protobuf: Boolean = true,
            additionalConfig: HttpClientConfig<*>.() -> Unit = {},
        ): HttpClient =
            HttpClient {
                defaultEngineConfig(protobuf)
                additionalConfig()
            }

        public fun <T : HttpClientEngineConfig> defaultHttpClient(
            engine: HttpClientEngineFactory<T>,
            protobuf: Boolean = true,
            additionalConfig: HttpClientConfig<T>.() -> Unit = {},
        ): HttpClient =
            HttpClient(engine) {
                defaultEngineConfig(protobuf)
                additionalConfig()
            }

        public fun defaultHttpClient(
            engine: HttpClientEngine,
            protobuf: Boolean = true,
            additionalConfig: HttpClientConfig<*>.() -> Unit = {},
        ): HttpClient =
            HttpClient(engine) {
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
        noinline requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
        cache: Boolean = true,
    ) = httpClient.request(url) {
        this@request.method = method
        setBody(body)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        attributes.put(Attributes.Cache, cache)
        requestBuilder?.invoke(this)
    }

    private suspend inline fun <reified T, reified R> defaultRequest(
        method: HttpMethod,
        url: Url,
        body: T,
        noinline requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
        cache: Boolean = true,
    ) = defaultRawRequest<T>(method, url, body, requestBuilder, cache).body<R>()

    public suspend fun getKnownRepositories(requestBuilder: (HttpRequestBuilder.() -> Unit)? = null): List<ApiRepository> {
        val apiRepositoryCache = cachedb.apiRepositoryCache()

        apiRepositoryCache.iterateAll()
            .firstOrNull()
            ?.let { if (isOffline() || !it.isExpired) return it.values }

        val results = httpClient.request(endpoints.knownRepositories) {
            method = HttpMethod.Get
            header(HttpHeaders.Accept, ContentType.Application.Json)
            requestBuilder?.invoke(this)
        }.body<List<ApiRepository>>()

        apiRepositoryCache.clear()
        apiRepositoryCache.insert(ApiRepositoryCacheEntry(results))
        return results
    }


    public suspend fun getPackageInfoByIds(
        ids: Set<String>,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): Map<String, ApiPackage> =
        fetchPackageInfo(ids, "id", endpoints.packageInfoByIds, requestBuilder)

    public suspend fun getPackageInfoByIdHashes(
        ids: Set<String>,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): Map<String, ApiPackage> =
        fetchPackageInfo(ids, "_id", endpoints.packageInfoByIds, requestBuilder)

    // Common Function to Fetch Package Info (Handles Both ID and ID Hash Retrieval)
    private suspend fun fetchPackageInfo(
        identifiers: Set<String>,
        lookupField: String,  // "_id" for idHash, "id" for id
        endpointUrl: Url,  // Differentiate between the two endpoints
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): Map<String, ApiPackage> = coroutineScope {

        val packageInfoCache = cachedb.apiPackagesCache()

        val cachedResults =
            identifiers
                .map { id ->
                    async {
                        packageInfoCache
                            .find(lookupField, id)
                            .firstOrNull()
                            ?.takeIf { isOffline() || !it.isExpired }
                            ?.let { id to it.apiPackage }  // Pair the ID with the cache entry for easier processing
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toMap()

        val unresolvedIdentifiers = identifiers - cachedResults.keys

        if (unresolvedIdentifiers.isEmpty()) {
            return@coroutineScope cachedResults
        }

        val onlineResults = defaultRequest<_, List<ApiPackage>>(
            method = HttpMethod.Post,
            url = endpointUrl,
            body = GetPackageInfoRequest(unresolvedIdentifiers),
            requestBuilder = requestBuilder,
        ).associateBy { it.id }

        onlineResults.values.forEach {
            packageInfoCache.insert(ApiPackageCacheEntry(it))
        }

        // Combine Results
        onlineResults + cachedResults
    }

    public suspend fun searchPackages(
        request: SearchPackagesRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): List<ApiPackage> {
        val searchCache= cachedb.searchPackageCache()
        searchCache.find("searchQuery", request.searchQuery)
            .filter { it.request.packagesType.any { it in request.packagesType } }
            //filter and delete the exiped one
            .toList()
            .partition { !it.isExpired }
            //find the most
//            .let { if (isOffline() || !it.isExpired) return it.request.searchQuery }

        defaultRequest<_, List<ApiPackage>>(
            method = HttpMethod.Post,
            url = endpoints.searchPackages,
            body = request,
            requestBuilder = requestBuilder,
        )

    }

    public suspend fun startScroll(
        request: SearchPackagesStartScrollRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): SearchPackagesScrollResponse =
        defaultRequest<_, SearchPackagesScrollResponse>(
            method = HttpMethod.Post,
            url = endpoints.startScroll,
            body = request,
            requestBuilder = requestBuilder,
        )

    public suspend fun nextScroll(
        request: SearchPackagesNextScrollRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): SearchPackagesScrollResponse =
        defaultRequest<_, SearchPackagesScrollResponse>(
            method = HttpMethod.Post,
            url = endpoints.nextScroll,
            body = request,
            requestBuilder = requestBuilder,
        )

    public suspend fun searchProjects(
        request: SearchProjectRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)?,
    ): List<ApiProject> =
        defaultRequest<_, List<ApiProject>>(
            method = HttpMethod.Post,
            url = endpoints.searchPackages,
            body = request,
            requestBuilder = requestBuilder,
        )

    public suspend fun refreshPackagesInfo(
        request: RefreshPackagesInfoRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): List<ApiPackage> =
        defaultRequest<_, List<ApiPackage>>(
            method = HttpMethod.Post,
            url = endpoints.refreshPackagesInfo,
            body = request,
            requestBuilder = requestBuilder,
        )

    public fun isOnlineFlow(pollingInterval: Duration = 1.minutes): Flow<Boolean> =
        flow {
            while (true) {
                val request =
                    kotlin.runCatching {
                        httpClient.get(endpoints.health) {
                            header(HttpHeaders.Accept, ContentType.Text.Plain)
                        }
                    }
                val isOnline =
                    request
                        .map { it.status.isSuccess() }
                        .getOrDefault(false)
                emit(isOnline)
                delay(pollingInterval)
            }
        }
}
