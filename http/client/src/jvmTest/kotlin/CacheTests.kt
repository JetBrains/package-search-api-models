import cache.ApiPackageCacheEntry
import cache.CacheDB
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiRepository
import org.jetbrains.packagesearch.api.v3.http.PackageSearchApiClient
import org.jetbrains.packagesearch.api.v3.http.PackageSearchEndpoints
import org.jetbrains.packagesearch.api.v3.http.RefreshPackagesInfoRequest
import org.jetbrains.packagesearch.api.v3.http.SearchPackagesRequest
import org.jetbrains.packagesearch.api.v3.search.PackagesType
import org.junit.jupiter.api.Test
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.toSet
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CacheTests {

    internal data class TestEnv<T>(
        val apiClient: PackageSearchApiClient,
        val mockEngine: MockEngine,
        val mockResponse: T,
        val db: CacheDB
    )

    @Test
    fun `PackageByID test Cache Hit`() = runTest(timeout = 10.seconds) {

        val (apiClient, mockEngine, mockResponse) =
            setupTestEnv<List<ApiPackage>>(resourceFilename = "package-info-by-ids-ktor.json")

        val packageId = mockResponse.first().id
        // First request (populates cache)
        val response1 = apiClient.getPackageInfoByIds(setOf(packageId))
        assert(response1.values.firstOrNull()!!.id == packageId)

        // Second request (should hit cache)
        val response2 = apiClient.getPackageInfoByIds(setOf(packageId))
        assert(response2.values.firstOrNull()!!.id == packageId)

        // Ensure the mock engine wasn't called a second time.
        val endpointsCalls =
            mockEngine.getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIds)

        mockEngine.close()
        assertEquals(1, endpointsCalls)
    }


    @Test
    fun `PackageByID test Cache Expired`() = runTest(timeout = 20.seconds) {

        val testEnv =
            setupTestEnv<List<ApiPackage>>(resourceFilename = "package-info-by-ids-ktor.json")

        val expiredDBEntry = ApiPackageCacheEntry(
            apiPackage = testEnv.mockResponse.first(),
            expires = Clock.System.now().minus(1.minutes),
        )

        testEnv.db.apiPackagesCache().insert(expiredDBEntry)

        val packageId = testEnv.mockResponse.first().id
        // First request (should refresh cache)
        val response1 = testEnv.apiClient.getPackageInfoByIds(setOf(packageId))
        assert(response1.values.firstOrNull()!!.id == packageId)

        // Ensure data has been retrieved from BE.
        assertEquals(1, testEnv.mockEngine.getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIds))


        // Second request (should hit cache)
        val response2 = testEnv.apiClient.getPackageInfoByIds(setOf(packageId))
        assert(response2.values.firstOrNull()!!.id == packageId)

        testEnv.mockEngine.close()
        assertEquals(1, testEnv.mockEngine.getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIds))

    }

    @Test
    fun `PackageByHash test Cache Hit`() = runTest(timeout = 10.seconds) {
        val (apiClient, mockEngine, mockResponse) =
            setupTestEnv<List<ApiPackage>>(resourceFilename = "package-info-by-ids-ktor.json")

        val packageIdHash = mockResponse.first().idHash
        // First request (populates cache)
        val response1 = apiClient.getPackageInfoByIdHashes(setOf(packageIdHash))
        assert(response1.values.firstOrNull()!!.idHash == packageIdHash)

        // Second request (should hit cache)
        val response2 = apiClient.getPackageInfoByIdHashes(setOf(packageIdHash))
        assert(response2.values.firstOrNull()!!.idHash == packageIdHash)

        // Ensure the mock engine wasn't called a second time.
        val endpointsCalls =
            mockEngine.getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIdHashes)

        mockEngine.close()
        assertEquals(1, endpointsCalls)
    }

    @Test
    fun `PackageById test cache not exhaustive`() = runTest(timeout = 10.seconds) {
        val (apiClient, mockEngine, mockResponse) =
            setupTestEnv<List<ApiPackage>>(resourceFilename = "package-info-by-ids-ktor.json")
        //create cache entry
        apiClient.getPackageInfoByIds(mockResponse.map { it.id }.toSet())

        apiClient.getPackageInfoByIds(mockResponse.map { it.id }.toSet() + "androidx.compose.runtime:runtime")

        val endpointsCalls =
            mockEngine.geRequestsFor(PackageSearchEndpoints.DEV.packageInfoByIds)

        mockEngine.close()
        assertEquals(2, endpointsCalls.size)
    }

    @Test
    fun `getKnownRepositories test Cache Hit`() = runTest(timeout = 10.seconds) {
        val (apiClient, mockEngine) = setupTestEnv<List<ApiRepository>>("known-repositories.json")

        val response1 = apiClient.getKnownRepositories()
        // Second request (should hit cache)
        val response2 = apiClient.getKnownRepositories()

        response1.map { it.id }.forEach() {
            assert(it in response2.map { it.id })
        }

        // Ensure the mock engine wasn't called a second time.
        val endpointsCalls =
            mockEngine.getRequestCount(PackageSearchEndpoints.DEV.knownRepositories)

        mockEngine.close()
        assertEquals(1, endpointsCalls)

    }

    @Test
    fun `searchPackages test Cache Hit`() = runTest(timeout = 10.seconds) {
        val (apiClient, mockEngine) = setupTestEnv<List<ApiPackage>>("package-info-by-ids-ktor.json")
        val queryString = "whatever"
        repeat(3) {
            apiClient.searchPackages(
                SearchPackagesRequest(
                    listOf(PackagesType.Maven),
                    searchQuery = queryString
                )
            )
        }
        val endpointsCalls =
            mockEngine.getRequestCount(PackageSearchEndpoints.DEV.searchPackages)

        mockEngine.close()
        assertEquals(1, endpointsCalls)

    }

    @Test
    fun `searchPackages non exhaustive cache`() = runTest(timeout = 10.seconds) {
        val (apiClient, mockEngine) = setupTestEnv<List<ApiPackage>>("package-info-by-ids-ktor.json")
        val queryString = "whatever"
        apiClient.searchPackages(
            SearchPackagesRequest(
                listOf(PackagesType.Maven),
                searchQuery = queryString
            )
        )
        apiClient.searchPackages(
            SearchPackagesRequest(
                listOf(PackagesType.Maven, PackagesType.Npm),
                searchQuery = queryString
            )
        )

        val endpointsCalls =
            mockEngine.getRequestCount(PackageSearchEndpoints.DEV.searchPackages)

        mockEngine.close()
        assertEquals(2, endpointsCalls)

    }

    @Test
    fun `refreshPackagesInfo smart cache`() = runTest(timeout = 10.seconds) {
        val (apiClient, mockEngine, mockResponse) = setupTestEnv<List<ApiPackage>>("package-info-by-ids-ktor.json")

        val refreshRequest = RefreshPackagesInfoRequest(
            listOf(
                RefreshPackagesInfoRequest.CacheRequest(
                    mockResponse.first().idHash
                )
            )
        )

        //ask to refresh package info for ktor hashID -> a new cache entry should be created or updated
        apiClient.refreshPackagesInfo(refreshRequest)

        //Note: delay will be skipped by Jupiter runTest function
        //we need to wait a small amount of time before we will find the cache updated
        Thread.sleep(500)

        apiClient.getPackageInfoByIds(setOf(mockResponse.first().id))

        val endpointsCalls =
            mockEngine.getRequestCount(PackageSearchEndpoints.DEV.packageInfoByIds)

        mockEngine.close()
        assertEquals(0, endpointsCalls)

    }


}





