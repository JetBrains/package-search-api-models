import cache.ApiPackageCacheEntry
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
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.document.database.mvstore.asDataStore
import kotlinx.serialization.json.Json
import org.h2.mvstore.MVStore
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.http.PackageSearchApiClient
import org.jetbrains.packagesearch.api.v3.http.PackageSearchEndpoints
import org.junit.jupiter.api.Test
import kotlin.collections.first
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CacheTests {


    @Test
    fun testCacheHit() = runTest(timeout = 10.seconds) {

        val jsonResponse = this::class.java.classLoader.getResource("package-info-by-ids-ktor.json")!!.readText()
        val mockResponse = Json.decodeFromString<List<ApiPackage>>(jsonResponse).first()

        val mockEngine = buildMockEngine(jsonResponse)

        // ApiClient coroutine scope will be created from httpClient coroutine context
        val apiClient = PackageSearchApiClient(
            httpClient =  setupHttpClient(mockEngine),
            endpoints = PackageSearchEndpoints.DEV,
            dataStore = MVStore.open(null).asDataStore(), //in memory db
        )

        // First request (populates cache)
        val response1 = apiClient.getPackageInfoByIds(setOf(mockResponse.id))
        assert(response1.values.firstOrNull()!!.id == mockResponse.id)

        // Second request (should hit cache)
        val response2 = apiClient.getPackageInfoByIds(setOf(mockResponse.id))
        assert(response2.values.firstOrNull()!!.id == mockResponse.id)

        // Ensure the mock engine wasn't called a second time.
        val endpointsCalls =
            mockEngine.getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIds)

        mockEngine.close()
        assertEquals(1, endpointsCalls)
    }


    @Test
    fun testCacheRefresh() = runTest(timeout = 20.seconds) {
        val jsonResponse = this::class.java.classLoader.getResource("package-info-by-ids-ktor.json")!!.readText()
        val mockResponse = Json.decodeFromString<List<ApiPackage>>(jsonResponse).first()

        val expiredDBEntry = ApiPackageCacheEntry(
            apiPackage = mockResponse,
            expires = Clock.System.now().minus(1.minutes),
        )

        val dataStore = MVStore.open(null).asDataStore()
        //setting up db
        val collection = CacheDB(dataStore).apiPackagesCache()
        collection.insert(expiredDBEntry)

        val mockEngine = buildMockEngine(jsonResponse)

        // ApiClient coroutine scope will be created from httpClient coroutine context
        val apiClient = PackageSearchApiClient(
            httpClient = setupHttpClient(mockEngine),
            endpoints = PackageSearchEndpoints.DEV,
            dataStore = dataStore,
        )

        // First request (should refresh cache)
        val response1 = apiClient.getPackageInfoByIds(setOf(mockResponse.id))
        assert(response1.values.firstOrNull()!!.id == mockResponse.id)

        // Ensure data has been retrieved from BE.
        assertEquals(1, mockEngine.getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIds))


        // Second request (should hit cache)
        val response2 = apiClient.getPackageInfoByIds(setOf(mockResponse.id))
        assert(response2.values.firstOrNull()!!.id == mockResponse.id)

        mockEngine.close()
        assertEquals(1, mockEngine.getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIds))

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


}





