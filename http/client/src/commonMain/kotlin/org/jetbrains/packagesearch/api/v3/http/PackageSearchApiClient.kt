package org.jetbrains.packagesearch.api.v3.http

import cache.ApiPackageCacheEntry
import cache.ApiProjectsCacheEntry
import cache.ApiRepositoryCacheEntry
import cache.CacheDB
import cache.SearchPackageRequestCacheEntry
import cache.SearchPackageScrollCacheEntry
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
import kotlin.collections.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

public class PackageSearchApiClient(
    dataStore: DataStore,
    private val httpClient: HttpClient = defaultHttpClient(),
    public val endpoints: PackageSearchEndpoints,
    private val coroutineScope: CoroutineScope = CoroutineScope(httpClient.coroutineContext),
    onlineCheckInterval: Duration = 1.minutes
) {

    private val cacheDB = CacheDB(dataStore)

    private val _onlineStateFlow = MutableStateFlow(true)
    public val onlineStateFlow: StateFlow<Boolean> = _onlineStateFlow

    init {
        initiateOnlineCheckJob(onlineCheckInterval)
    }


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
        val apiRepositoryCache = cacheDB.apiRepositoryCache()
        val isOffline = !onlineStateFlow.value
        val cachedResult = apiRepositoryCache.iterateAll().firstOrNull()

        cachedResult?.let { if (isOffline || !it.isExpired) return it.values }

        val results = httpClient.request(endpoints.knownRepositories) {
            method = HttpMethod.Get
            header(HttpHeaders.Accept, ContentType.Application.Json)
            requestBuilder?.invoke(this)
        }.body<List<ApiRepository>>()

        apiRepositoryCache.insert(ApiRepositoryCacheEntry(cachedResult?._id, results))
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
        fetchPackageInfo(ids, "idHash", endpoints.packageInfoByIds, requestBuilder)

    // Common Function to Fetch Package Info (Handles Both ID and ID Hash Retrieval)
    private suspend fun fetchPackageInfo(
        identifiers: Set<String>,
        lookupField: String,  // "idHash" or "id"
        endpointUrl: Url,  // Differentiate between the two endpoints
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): Map<String, ApiPackage> = coroutineScope {

        val apiPackageCacheDB = cacheDB.apiPackagesCache()
        val isOffline = !onlineStateFlow.value

        val cachedResults =
            identifiers
                .map { id -> // NOTE: id depends on the lookupField
                    async {
                        apiPackageCacheDB
                            .find(lookupField, id)
                            .firstOrNull()
                            ?.let { id to it }  // Pair the ID with the cache entry for easier processing
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toMap()

        val validResults = if (!isOffline) cachedResults.filter { !it.value.isExpired } else cachedResults

        val unresolvedIdentifiers = identifiers - validResults.keys

        if (unresolvedIdentifiers.isEmpty()) {
            return@coroutineScope validResults.mapValues { it.value.apiPackage }
        }

        val onlineResults = defaultRequest<_, List<ApiPackage>>(
            method = HttpMethod.Post,
            url = endpointUrl,
            body = GetPackageInfoRequest(unresolvedIdentifiers),
            requestBuilder = requestBuilder,
        ).associateBy { if (lookupField == "id") it.id else it.idHash }



        onlineResults.values.forEach {

            val cacheKey = if (lookupField == "id") it.id else it.idHash

            apiPackageCacheDB.insert(
                ApiPackageCacheEntry(
                    _id = cachedResults[cacheKey]?._id,
                    it
                )
            )
        }

        // Combine Results
        onlineResults + cachedResults.mapValues { it.value.apiPackage }
    }

    public suspend fun searchPackages(
        request: SearchPackagesRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): List<ApiPackage> {
        val searchCache = cacheDB.searchPackageCache()

        val isOffline = !onlineStateFlow.value
        val searchResult = searchCache.find("searchQuery", request.searchQuery)
            .firstOrNull()

        searchResult
            ?.takeIf { isOffline || !it.isExpired }
            ?.takeIf { it.request.packagesType.toSet().containsAll(request.packagesType.toSet()) }
            ?.let { return it.packages }

        // cache result not found or expired or not exhaustive enough

        return defaultRequest<_, List<ApiPackage>>(
            method = HttpMethod.Post,
            url = endpoints.searchPackages,
            body = request,
            requestBuilder = requestBuilder,
        ).also {
            searchCache.insert(SearchPackageRequestCacheEntry(searchResult?._id, request, it))
        }

    }

    public suspend fun startScroll(
        request: SearchPackagesStartScrollRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): SearchPackagesScrollResponse {
        val cache = cacheDB.scrollStartPackageCache()

        val cacheResult = cache
            .find("searchQuery", request.searchQuery)
            .firstOrNull()
            ?.also {
                if (!it.isExpired) return SearchPackagesScrollResponse(it.scrollId, it.packages)
            }

        return defaultRequest<_, SearchPackagesScrollResponse>(
            method = HttpMethod.Post,
            url = endpoints.startScroll,
            body = request,
            requestBuilder = requestBuilder,
        ).also {
            cache.insert(
                SearchPackageScrollCacheEntry(
                    _id = cacheResult?._id,
                    scrollId = it.scrollId,
                    packages = it.data
                )
            )
        }
    }

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
    ): List<ApiProject> {
        val apiProjectsCache = cacheDB.apiProjectsCache()

        val cacheResult = apiProjectsCache.find("queryString", request.query)
            .firstOrNull()
            ?.also { if (!it.isExpired) return it.values }

        return defaultRequest<_, List<ApiProject>>(
            method = HttpMethod.Post,
            url = endpoints.searchPackages,
            body = request,
            requestBuilder = requestBuilder,
        ).also {
            apiProjectsCache.insert(
                ApiProjectsCacheEntry(
                    _id = cacheResult?._id,
                    queryString = request.query,
                    values = it
                )
            )
        }

    }

    public suspend fun refreshPackagesInfo(
        request: RefreshPackagesInfoRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): List<ApiPackage> {
        //no caches for this endpoint
        val results =
            defaultRequest<_, List<ApiPackage>>(
                method = HttpMethod.Post,
                url = endpoints.refreshPackagesInfo,
                body = request,
                requestBuilder = requestBuilder,
            )

        // update ApiPackageCache
        coroutineScope.launch(Dispatchers.IO) {
            val apiPackageCacheDB = cacheDB.apiPackagesCache()
            val updates =
                results
                    .map { result ->
                        async {
                            apiPackageCacheDB
                                .find("id", result.id)
                                .firstOrNull()
                                ?.let { it._id to result }  // Pair the ID with the cache entry for easier processing
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
                    .toMap()
            updates.forEach {
                apiPackageCacheDB.insert(ApiPackageCacheEntry(it.key, it.value))
            }
            val updatedIDS = updates.map { it.value.id }
            results.filter { it.id !in updatedIDS }.forEach {
                apiPackageCacheDB.insert(ApiPackageCacheEntry(apiPackage = it))
            }
        }
        return results

    }

    private suspend fun checkOnlineState(): Boolean {
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
        return isOnline
    }

    private fun initiateOnlineCheckJob(onlineCheckInterval: Duration) {
        coroutineScope.launch(Dispatchers.IO) {
            while (coroutineScope.isActive) {
                _onlineStateFlow.emit(checkOnlineState())
                delay(onlineCheckInterval)
            }
        }
    }

}


