package com.jetbrains.packagesearch.api.v1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    @SerialName("error") val error: ApiError
) {

    @Serializable
    data class ApiError(
        @SerialName("message") val message: String
    )

    companion object {

        fun with(message: String) = ApiErrorResponse(ApiError(message))
    }
}
