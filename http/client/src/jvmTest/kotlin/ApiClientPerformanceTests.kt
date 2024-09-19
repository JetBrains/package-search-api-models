import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.document.database.mvstore.asDataStore
import org.h2.mvstore.MVStore
import org.jetbrains.packagesearch.api.v3.http.PackageSearchApiClient
import org.jetbrains.packagesearch.api.v3.http.PackageSearchEndpoints
import org.jetbrains.packagesearch.api.v3.http.requestTimeout
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue


class ApiClientPerformanceTests {

    companion object {
        internal val timingMap = mutableMapOf<String, Long>()

        @Suppress("RedundantUnitReturnType")
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
        httpClient = HttpClient(Java) {
            defaultRequest {
                header("JB-Plugin-Version", "241.0.12")
            }
            install(ContentNegotiation) {
                json()
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
            install(Logging) {
                level = LogLevel.ALL
            }
        },

        )

    private val sampleIdHashes = setOf(
        "afa0f79b67522a855aa1343ed55939170e5910020c7e39f081c59f38f132c0cf", //ktor
        "e0644eb2501825154b2fc68467537bd8d4b460f2df6aab7b7b2051af793232db", //google guava bom
        "ad25fb431b508c516a20bf717e144c96b494492f1fceaf7b82778bc1f24b39cc", //amazon kinesis client
        "828bfa41ee3e7021583716feaa714647e4ba0c850342e298abe71245f6f2dd4a" //docker-java
    )


    @Test
    fun `search single package `() = runTest {
        val (apiPackages, duration) = measureTimedValue {
            apiClient.getPackageInfoByIdHashes(idHashes = setOf(sampleIdHashes.first()))
        }
        assert(apiPackages.isNotEmpty())
        timingMap["single package CF"] = duration.inWholeMilliseconds

    }

    @Test
    fun `search multiple package cloudFront`() = runTest {

        val (apiPackages, duration) = measureTimedValue {
            apiClient.getPackageInfoByIdHashes(idHashes = sampleIdHashes)
        }
        assert(apiPackages.keys.size == sampleIdHashes.size)
        assert(apiPackages.entries.isNotEmpty())
        timingMap["multiple packages (${sampleIdHashes.size}) CF"] = duration.inWholeMilliseconds
    }

    @Test
    fun `search single package on cloudFront that does not exist`() = runTest {
        val (apiPackages, duration) = measureTimedValue {
            apiClient.getPackageInfoByIdHashes(idHashes = setOf("invalid-hash"))
        }
        assert(apiPackages.isEmpty())
        timingMap["not found package CF"] = duration.inWholeMilliseconds
    }

}