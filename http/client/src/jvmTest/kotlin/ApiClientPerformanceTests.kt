import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.test.runTest
import kotlinx.document.database.mvstore.asDataStore
import org.h2.mvstore.MVStore
import org.jetbrains.packagesearch.api.v3.http.PackageSearchApiClient
import org.jetbrains.packagesearch.api.v3.http.PackageSearchEndpoints
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import kotlin.time.measureTimedValue


class ApiClientPerformanceTests {

    companion object {
        internal val timingMap = mutableMapOf<String, Long>()

        @JvmStatic
        @AfterAll
        fun collectTimings(): Unit {
            println("Timings:")
            timingMap.forEach { (key, value) ->
                println("$key: $value ms")
            }
        }
    }

    private val apiClient = PackageSearchApiClient(
        dataStore = MVStore.open(null).asDataStore(),
        endpoints = PackageSearchEndpoints.PROD,
        httpClient = HttpClient(engine = TODO("add Java Client engine"))
    )


    @Test
    fun test() {
        println("Test")
        assert(true)
    }

    @Test
    fun `search single package`() = runTest {
        val packagesIds = setOf(
            "afa0f79b67522a855aa1343ed55939170e5910020c7e39f081c59f38f132c0cf" //ktor
        )
        val (apiPackages, duration) = measureTimedValue {
            apiClient.getPackageInfoByIds(ids = packagesIds)
        }
        assert(apiPackages.isNotEmpty())
        timingMap["single package"] = duration.inWholeMilliseconds

    }

    @Test
    fun `search multiple package`() = runTest {
        val packagesIds = setOf(
            "afa0f79b67522a855aa1343ed55939170e5910020c7e39f081c59f38f132c0cf", //ktor
            "e0644eb2501825154b2fc68467537bd8d4b460f2df6aab7b7b2051af793232db", //google guava bom
            "ad25fb431b508c516a20bf717e144c96b494492f1fceaf7b82778bc1f24b39cc", //amazon kinesis client
            "828bfa41ee3e7021583716feaa714647e4ba0c850342e298abe71245f6f2dd4a" //docker-java
        )
        val (apiPackages, duration) = measureTimedValue {
            apiClient.getPackageInfoByIds(ids = packagesIds)
        }
        assert(apiPackages.keys.size == packagesIds.size)
        assert(apiPackages.entries.isNotEmpty())
        timingMap["multiple packages"] = duration.inWholeMilliseconds
    }


    @Test
    fun `search single package cloudFront`() = runTest {
        val packagesIds = setOf(
            "afa0f79b67522a855aa1343ed55939170e5910020c7e39f081c59f38f132c0cf" //ktor
        )
        val (apiPackages, duration) = measureTimedValue {
            apiClient.getPackageInfoByIds(ids = packagesIds)
        }
        assert(apiPackages.isNotEmpty())
        timingMap["single package cloudFront"] = duration.inWholeMilliseconds

    }

    @Test
    fun `search multiple package cloudFront`() = runTest {
        val packagesIds = setOf(
            "afa0f79b67522a855aa1343ed55939170e5910020c7e39f081c59f38f132c0cf", //ktor
            "e0644eb2501825154b2fc68467537bd8d4b460f2df6aab7b7b2051af793232db", //google guava bom
            "ad25fb431b508c516a20bf717e144c96b494492f1fceaf7b82778bc1f24b39cc", //amazon kinesis client
            "828bfa41ee3e7021583716feaa714647e4ba0c850342e298abe71245f6f2dd4a" //docker-java
        )
        val (apiPackages, duration) = measureTimedValue {
            apiClient.getPackageInfoByIds(ids = packagesIds)
        }
        assert(apiPackages.keys.size == packagesIds.size)
        assert(apiPackages.entries.isNotEmpty())
        timingMap["multiple packages cloudFront"] = duration.inWholeMilliseconds
    }

}