import CacheTests.TestEnv
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.TestScope
import kotlinx.document.database.mvstore.asDataStore
import kotlinx.serialization.json.Json
import org.h2.mvstore.MVStore
import org.jetbrains.packagesearch.api.v3.http.PackageSearchApiClient
import org.jetbrains.packagesearch.api.v3.http.PackageSearchEndpoints

private fun buildGetRequestUrl(endpoint: Url, queryParameters: Map<String, String>): Url =
    URLBuilder(url = endpoint).apply {
        queryParameters.forEach {
            parameters.append(it.key, it.value)
        }
    }.build()


internal fun MockEngine.geRequestsFor(
    url: Url,
    queryParameters: Map<String, String>
): List<HttpRequestData> {
    val encodedUrl = if (queryParameters.isEmpty()) url else buildGetRequestUrl(url, queryParameters)
    return requestHistory.filter { it.url == encodedUrl }
}


/**
 * Filters the request history to retrieve requests with the specified URL.
 * This comparison will exclude query parameters.
 *
 * @param url The URL to filter the request history by.
 * @return A list of requests that match the specified URL.
 */
internal fun MockEngine.geRequestsFor(url: Url) =
    requestHistory.filter { it.url.encodedPath == url.encodedPath }

/**
 * Returns the count of requests made to the mocked engine for the specified URL.
 * This comparison will exclude query parameters.
 *
 * @param filterUrl The URL to filter the request history by.
 * @return The count of requests made to the mocked engine for the specified URL.
 */
internal fun MockEngine.getRequestCount(filterUrl: Url) =
    geRequestsFor(filterUrl).size

/**
 * Returns the count of requests made to the MockEngine that match the given filter URL and query parameters.
 *
 * @param idHash The id hash value used to further filter the requests.
 * @return The count of requests that match the filter URL and query parameters.
 */
internal fun MockEngine.requestCountForGetIdHash(idHash: String) =
    getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIdHash, mapOf("idHash" to idHash))

/**
 * Returns the count of requests made to the MockEngine that match the given filter URL and query parameters.
 *
 * @param filterUrl The URL used to filter the requests.
 * @param queryParameters The query parameters used to further filter the requests (default is an empty map).
 * @return The count of requests that match the filter URL and query parameters.
 */
internal fun MockEngine.getRequestCount(filterUrl: Url, queryParameters: Map<String, String> = emptyMap()) =
    geRequestsFor(filterUrl, queryParameters).size


internal inline fun <reified T> TestScope.setupTestEnv(
    resourceFilename: String,
): TestEnv<T> {
    val jsonResponse = this::class.java.classLoader.getResource(resourceFilename)!!.readText()
    val mockResponse = Json.decodeFromString<T>(jsonResponse)

    val mockEngine = buildMockEngine(jsonResponse)

    val apiClient = PackageSearchApiClient(
        httpClient = setupHttpClient(mockEngine),
        endpoints = PackageSearchEndpoints.DEV,
        dataStore = MVStore.open(null).asDataStore(), //in memory db
    )

    return TestEnv<T>(
        apiClient = apiClient,
        mockEngine = mockEngine,
        mockResponse = mockResponse,
    )
}

private fun setupHttpClient(mockEngine: MockEngine): HttpClient = HttpClient(mockEngine) {
    install(ContentNegotiation) {
        json()
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
}

private fun buildMockEngine(jsonResponse: String): MockEngine = MockEngine {
    respond(
        content = jsonResponse,
        status = HttpStatusCode.OK,
        headers = headers {
            append(HttpHeaders.ContentType, "application/json")
        }
    )
}