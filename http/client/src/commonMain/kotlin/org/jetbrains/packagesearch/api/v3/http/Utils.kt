package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.search.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public fun buildUrl(action: URLBuilder.() -> Unit): Url = URLBuilder().apply(action).build()

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

public fun HttpRequest.toSerializable(): SerializableHttpRequest =
    SerializableHttpRequest(url.toString(), method.value, headers.toMap())

public typealias EmptyBody = String
