package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: Error,
) {

    @Serializable
    data class Error(
        val message: String,
        val stackTrace: List<String> = emptyList(),
    )

    companion object {

        fun with(message: String, stackTrace: List<String> = emptyList()) =
            ErrorResponse(Error(message, stackTrace))
    }
}
