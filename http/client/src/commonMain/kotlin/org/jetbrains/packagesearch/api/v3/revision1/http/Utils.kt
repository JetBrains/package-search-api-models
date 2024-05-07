package org.jetbrains.packagesearch.api.v3.revision1.http

import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.HttpRequest
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.util.toMap
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.revision1.ApiPackage
import org.jetbrains.packagesearch.api.v3.revision1.search.NextScrollParametersBuilder
import org.jetbrains.packagesearch.api.v3.revision1.search.SearchParametersBuilder
import org.jetbrains.packagesearch.api.v3.revision1.search.StartScrollParametersBuilder
import org.jetbrains.packagesearch.api.v3.revision1.search.buildNextScrollParameters
import org.jetbrains.packagesearch.api.v3.revision1.search.buildSearchParameters
import org.jetbrains.packagesearch.api.v3.revision1.search.buildStartScrollParameters
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal fun HttpMessageBuilder.header(
    key: String,
    vararg values: Any?,
) {
    headers.append(key, values.joinToString())
}

internal var HttpTimeout.HttpTimeoutCapabilityConfiguration.requestTimeout: Duration?
    get() = requestTimeoutMillis?.milliseconds
    set(value) {
        requestTimeoutMillis = value?.inWholeMilliseconds
    }

public suspend fun PackageSearchApiClient.searchPackages(builder: SearchParametersBuilder.() -> Unit): List<ApiPackage> =
    searchPackages(buildSearchParameters(builder))

public suspend fun PackageSearchApiClient.startScroll(builder: StartScrollParametersBuilder.() -> Unit): SearchPackagesScrollResponse =
    startScroll(buildStartScrollParameters(builder))

public suspend fun PackageSearchApiClient.nextScroll(builder: NextScrollParametersBuilder.() -> Unit): SearchPackagesScrollResponse =
    nextScroll(buildNextScrollParameters(builder))

@Serializable
public data class SerializableHttpStatusCode(val value: Int, val description: String)

public fun HttpStatusCode.toSerializable(): SerializableHttpStatusCode = SerializableHttpStatusCode(value, description)

@Serializable
public data class SerializableHttpRequest(
    val url: String,
    val method: String,
    val headers: Map<String, List<String>>,
)

public fun HttpRequest.toSerializable(): SerializableHttpRequest = SerializableHttpRequest(url.toString(), method.value, headers.toMap())
