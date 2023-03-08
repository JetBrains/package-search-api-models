package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    @SerialName("error") val error: Error
) {

    @Serializable
    data class Error(
        @SerialName("message") val message: String,
        @SerialName("stack_trace") val stackTrace: List<String> = emptyList()
    )

    companion object {

        fun with(message: String, stackTrace: List<String> = emptyList()) =
            ErrorResponse(Error(message, stackTrace))
    }
}
