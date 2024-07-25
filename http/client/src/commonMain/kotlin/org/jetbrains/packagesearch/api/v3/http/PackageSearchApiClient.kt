package org.jetbrains.packagesearch.api.v3.http

import cache.CacheEntry
import cache.DEFAULT_EXPIRATION_TIME
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
import kotlinx.document.database.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiProject
import org.jetbrains.packagesearch.api.v3.ApiRepository
import kotlin.collections.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal typealias SearchCacheEntry = CacheEntry<SearchPackagesRequest, List<ApiPackage>>
internal typealias PackageCacheEntry = CacheEntry<String, ApiPackage?>
internal typealias ScrollRequestCacheEntry = CacheEntry<SearchPackagesStartScrollRequest, SearchPackagesScrollResponse>
internal typealias ApiProjectCacheEntry = CacheEntry<SearchProjectRequest, List<ApiProject>>
internal typealias MiscellaneousCacheEntry = CacheEntry<String, JsonElement>

public class PackageSearchApiClient(
    dataStore: DataStore,
    private val httpClient: HttpClient = defaultHttpClient(),
    public val endpoints: PackageSearchEndpoints,
    coroutineScope: CoroutineScope = CoroutineScope(httpClient.coroutineContext),
    onlineCheckInterval: Duration = 1.minutes,
    public val cacheDuration: Duration = DEFAULT_EXPIRATION_TIME,
) {

    private val kotlinxDb = KotlinxDocumentDatabase(dataStore)

    internal val searchCacheCollection = coroutineScope.async {
        kotlinxDb.getObjectCollection<SearchCacheEntry>("SearchRequests")
            .apply { createIndex(SearchCacheEntry::key.name) }
    }

    internal val repositoriesCacheCollection = coroutineScope.async {
        kotlinxDb.getObjectCollection<MiscellaneousCacheEntry>("Miscellaneous")
            .apply { createIndex(MiscellaneousCacheEntry::key.name) }
    }

    internal val packagesCacheCollection = coroutineScope.async {
        kotlinxDb.getObjectCollection<PackageCacheEntry>("Packages")
            .apply { createIndex(PackageCacheEntry::key.name) }
    }

    internal val scrollRequestCacheCollection = coroutineScope.async {
        kotlinxDb.getObjectCollection<ScrollRequestCacheEntry>("StartScrollRequests")
            .apply { createIndex(ScrollRequestCacheEntry::key.name) }
    }

    internal val apiProjectCacheCollection = coroutineScope.async {
        kotlinxDb.getObjectCollection<ApiProjectCacheEntry>("ApiProjects")
            .apply { createIndex(ApiProjectCacheEntry::key.name) }
    }


    public val onlineStateFlow: StateFlow<Boolean> =
        flow {
            while (true) {
                val result = httpClient.get(endpoints.health) {
                    header(HttpHeaders.Accept, ContentType.Text.Plain)
                }
                emit(result.status.isSuccess())
                delay(onlineCheckInterval)
            }
        }
            .retry()
            .stateIn(coroutineScope, SharingStarted.Lazily, true)

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
        val apiRepositoryCache = repositoriesCacheCollection.await()
        val isOffline = !onlineStateFlow.value
        val cachedResult = apiRepositoryCache.find(
            selector = MiscellaneousCacheEntry::key.name,
            value = "repositories"
        ).singleOrNull()

        cachedResult?.let {
            if (isOffline || !it.isExpired()) {
                return apiRepositoryCache.json.decodeFromJsonElement<List<ApiRepository>>(it.value)
            }

        }

        val results = httpClient.request(endpoints.knownRepositories) {
            method = HttpMethod.Get
            header(HttpHeaders.Accept, ContentType.Application.Json)
            requestBuilder?.invoke(this)
        }.body<List<ApiRepository>>()

        apiRepositoryCache.updateWhere(
            fieldSelector = MiscellaneousCacheEntry::key.name,
            fieldValue = "repositories",
            upsert = true,
            update = MiscellaneousCacheEntry(
                key = "repositories",
                value = apiRepositoryCache.json.encodeToJsonElement(results)
            )
        )

        return results
    }

    public suspend fun getPackageInfoByIds(
        ids: Set<String>,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): Map<String, ApiPackage> =
        getPackageInfoByIdHashes(
            idHashes = ids.map { ApiPackage.hashPackageId(it) }.toSet(),
            useHashes = false,
            requestBuilder = requestBuilder
        )

    public suspend fun getPackageInfoByIdHashes(
        idHashes: Set<String>,
        useHashes: Boolean = true,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): Map<String, ApiPackage> = coroutineScope {

        val packagesCache = packagesCacheCollection.await()

        val isOffline = !onlineStateFlow.value

        val cachedResults =
            idHashes
                .map { id -> // NOTE: id depends on the lookupField
                    async {
                        packagesCache
                            .find(PackageCacheEntry::key.name, value = id)
                            .firstOrNull()
                    }
                }
                .awaitAll()
                .filterNotNull()

        val cachedResultMap = cachedResults
            .mapNotNull { it.value }
            .associateBy { if (useHashes) it.idHash else it.id }
        val validResults =
            when {
                !isOffline -> cachedResults.filter { !it.isExpired(cacheDuration) }
                else -> return@coroutineScope cachedResultMap
            }

        val resultIds = validResults.map { it.key }

        val unresolvedIdentifiers =
            idHashes - resultIds

        if (unresolvedIdentifiers.isEmpty()) {
            return@coroutineScope validResults
                .mapNotNull { it.value }
                .associateBy { if (useHashes) it.idHash else it.id }
        }

        val onlineResults = defaultRequest<_, List<ApiPackage>>(
            method = HttpMethod.Post,
            url = endpoints.packageInfoByIdHashes,
            body = GetPackageInfoRequest(unresolvedIdentifiers),
            requestBuilder = requestBuilder,
        ).associateBy { it.idHash }

        val notFoundPackages = idHashes - cachedResults.map { it.key }.toSet() - onlineResults.keys

        notFoundPackages.forEach {
            packagesCache.updateWhere(
                fieldSelector = PackageCacheEntry::key.name,
                fieldValue = it,
                upsert = true,
                update = PackageCacheEntry(it, null)
            )
        }

        onlineResults.values.forEach {
            packagesCache.updateWhere(
                fieldSelector = PackageCacheEntry::key.name,
                fieldValue = it.idHash,
                upsert = true,
                update = PackageCacheEntry(it.idHash, it)
            )
        }

        val onlineResultsMappedKeys = onlineResults.mapKeys { if (useHashes) it.key else it.value.id }

        // Map Combine Results
        onlineResultsMappedKeys + cachedResultMap
    }

    public suspend fun searchPackages(
        request: SearchPackagesRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): List<ApiPackage> {
        val isOffline = !onlineStateFlow.value

        val searchCache = searchCacheCollection.await()

        searchCache.find(selector = SearchCacheEntry::key.name, value = request)
            .firstOrNull()
            ?.takeIf { isOffline || !it.isExpired() }
            ?.also { return it.value }

        // cache result not found or expired or not exhaustive enough

        return defaultRequest<_, List<ApiPackage>>(
            method = HttpMethod.Post,
            url = endpoints.searchPackages,
            body = request,
            requestBuilder = requestBuilder,
        ).also { newPackages ->
            searchCache.updateWhere(
                fieldSelector = SearchCacheEntry::key.name,
                fieldValue = request,
                upsert = true,
                update = SearchCacheEntry(request, newPackages)
            )
            val packagesCache = packagesCacheCollection.await()
            newPackages.forEach {
                packagesCache.updateWhere(
                    fieldSelector = PackageCacheEntry::key.name,
                    fieldValue = it.idHash,
                    upsert = true,
                    update = PackageCacheEntry(it.idHash, it)
                )
            }
        }

    }

    public suspend fun startScroll(
        request: SearchPackagesStartScrollRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): SearchPackagesScrollResponse {

        val scrollCache = scrollRequestCacheCollection.await()
        scrollCache.find(selector = ScrollRequestCacheEntry::key.name, value = request)
            .firstOrNull()
            ?.also {
                if (!it.isExpired()) return it.value
            }

        return defaultRequest<_, SearchPackagesScrollResponse>(
            method = HttpMethod.Post,
            url = endpoints.startScroll,
            body = request,
            requestBuilder = requestBuilder,
        ).also { it ->
            scrollCache.updateWhere(
                fieldSelector = ScrollRequestCacheEntry::key.name,
                fieldValue = request,
                upsert = true,
                update = ScrollRequestCacheEntry(request, it)
            )
            val packagesCache = packagesCacheCollection.await()
            it.data.forEach {
                packagesCache.updateWhere(
                    fieldSelector = PackageCacheEntry::key.name,
                    fieldValue = it.idHash,
                    upsert = true,
                    update = PackageCacheEntry(it.idHash, it)
                )
            }
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

        val apiProjectsCache = apiProjectCacheCollection.await()

        apiProjectsCache.find(selector = SearchCacheEntry::key.name, value = request).firstOrNull()
            ?.also { if (!it.isExpired()) return it.value }

        return defaultRequest<_, List<ApiProject>>(
            method = HttpMethod.Post,
            url = endpoints.searchPackages,
            body = request,
            requestBuilder = requestBuilder,
        ).also {
            apiProjectsCache.updateWhere(
                fieldSelector = ApiProjectCacheEntry::key.name,
                fieldValue = request,
                upsert = true,
                update = ApiProjectCacheEntry(request, it)
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

        val packagesCache = packagesCacheCollection.await()
        results.forEach {
            packagesCache.updateWhere(
                fieldSelector = PackageCacheEntry::key.name,
                fieldValue = it.idHash,
                upsert = true,
                update = PackageCacheEntry(it.idHash, it)
            )
        }
        return results

    }

}

