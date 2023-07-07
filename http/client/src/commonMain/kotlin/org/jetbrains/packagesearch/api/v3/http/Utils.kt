package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.*

public suspend fun PackageSearchApiClient.getScmByUrl(urls: List<String>): String? =
    getScmByUrl(GetScmByUrlRequest(urls))

public fun buildUrl(action: URLBuilder.() -> Unit): Url = URLBuilder().apply(action).build()

internal fun HttpMessageBuilder.header(key: String, vararg values: Any?) {
    headers.append(key, values.joinToString())
}