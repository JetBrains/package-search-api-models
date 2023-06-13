package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.URLBuilder

suspend fun PackageSearchApiClient.getScmByUrl(urls: List<String>): String? =
    getScmByUrl(GetScmByUrlRequest(urls))

fun buildUrl(action: URLBuilder.() -> Unit) = URLBuilder().apply(action).build()
