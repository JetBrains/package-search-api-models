package org.jetbrains.packagesearch.api.v3.http

import kotlinx.serialization.Serializable

@Serializable
public data class PackageSearchApiException(
    val serverMessage: String,
    val request: SerializableHttpRequest,
    val statusCode: SerializableHttpStatusCode,
    val remoteStackTrace: List<String> = emptyList(),
) : Throwable() {
    override val message: String
        get() =
            buildString {
                append("Error response for endpoint ${request.method} ${request.url}:")
                appendLine("- Headers:")
                request.headers.forEach { appendLine("  -> ${it.key}: ${it.value.joinToString()}") }
                appendLine("- Status code: ${statusCode.value} ${statusCode.description}")
                if (remoteStackTrace.isNotEmpty()) {
                    append("- Remote stack trace:")
                    remoteStackTrace.forEach { appendLine("    $it") }
                }
            }
}
