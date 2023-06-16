package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    @SerialName("error") val error: ApiError,
) {

    @Serializable
    data class ApiError(
        @SerialName("message") val message: String,
        @SerialName("stack_trace") val stackTrace: List<String> = emptyList(),
    )

    companion object {

        fun with(message: String, stackTrace: List<String> = emptyList()) =
            ApiErrorResponse(ApiError(message, stackTrace))
    }
}
