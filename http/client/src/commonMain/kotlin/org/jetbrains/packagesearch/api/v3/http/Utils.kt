package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.plugins.*
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.search.NextScrollParametersBuilder
import org.jetbrains.packagesearch.api.v3.search.SearchParametersBuilder
import org.jetbrains.packagesearch.api.v3.search.StartScrollParametersBuilder
import org.jetbrains.packagesearch.api.v3.search.buildNextScrollParameters
import org.jetbrains.packagesearch.api.v3.search.buildSearchParameters
import org.jetbrains.packagesearch.api.v3.search.buildStartScrollParameters

public fun buildUrl(action: URLBuilder.() -> Unit): Url = URLBuilder().apply(action).build()

internal fun HttpMessageBuilder.header(key: String, vararg values: Any?) {
    headers.append(key, values.joinToString())
}

internal var HttpTimeout.HttpTimeoutCapabilityConfiguration.requestTimeout: Duration?
    get() = requestTimeoutMillis?.milliseconds
    set(value) { requestTimeoutMillis = value?.inWholeMilliseconds }

internal fun HttpRequestRetry.Configuration.constantDelay(
    delay: Duration = 1.seconds,
    randomization: Duration = 1.seconds,
    respectRetryAfterHeader: Boolean = true
) {
    constantDelay(delay.inWholeMilliseconds, randomization.inWholeMilliseconds, respectRetryAfterHeader)
}

public fun HttpRequestRetry.Configuration.retryIfNot(
    maxRetries: Int = -1,
    block: HttpRequestRetry.ShouldRetryContext.(HttpRequest, HttpResponse) -> Boolean,
) {
    retryIf(maxRetries) { request, response -> !block(request, response) }
}

public suspend fun PackageSearchApiClient.searchPackages(builder: SearchParametersBuilder.() -> Unit): List<ApiPackage> =
    searchPackages(buildSearchParameters(builder))

public suspend fun PackageSearchApiClient.startScroll(builder: StartScrollParametersBuilder.() -> Unit): SearchPackagesScrollResponse =
    startScroll(buildStartScrollParameters(builder))

public suspend fun PackageSearchApiClient.nextScroll(builder: NextScrollParametersBuilder.() -> Unit): SearchPackagesScrollResponse =
    nextScroll(buildNextScrollParameters(builder))