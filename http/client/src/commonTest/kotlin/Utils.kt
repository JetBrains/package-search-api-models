import CacheTests.TestEnv
import cache.CacheDB
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.TestScope
import kotlinx.document.database.mvstore.asDataStore
import kotlinx.serialization.json.Json
import org.h2.mvstore.MVStore
import org.jetbrains.packagesearch.api.v3.http.PackageSearchApiClient
import org.jetbrains.packagesearch.api.v3.http.PackageSearchEndpoints


fun MockEngine.geRequestsFor(url: Url) =
    requestHistory.filter { it.url == url }

fun MockEngine.getRequestCount(filterUrl: Url) =
    geRequestsFor(filterUrl).size


internal inline fun <reified T> TestScope.setupTestEnv(
    resourceFilename: String,
): TestEnv<T> {
    val jsonResponse = this::class.java.classLoader.getResource(resourceFilename)!!.readText()
    val mockResponse = Json.decodeFromString<T>(jsonResponse)

    val mockEngine = buildMockEngine(jsonResponse)

    val dataStore = MVStore.open(null).asDataStore()

    val apiClient = PackageSearchApiClient(
        httpClient = setupHttpClient(mockEngine),
        endpoints = PackageSearchEndpoints.DEV,
        dataStore = MVStore.open(null).asDataStore(), //in memory db
    )

    return TestEnv<T>(
        apiClient = apiClient,
        mockEngine = mockEngine,
        mockResponse = mockResponse,
        db = CacheDB(dataStore)
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