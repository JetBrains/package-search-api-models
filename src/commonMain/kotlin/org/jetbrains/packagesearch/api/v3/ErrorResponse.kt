package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.Serializable

@Serializable
public data class ErrorResponse(
    public val error: Error,
) {
    @Serializable
    public data class Error(
        public val message: String,
        public val stackTrace: List<String> = emptyList(),
    )

    public companion object {
        public fun with(
            message: String,
            stackTrace: List<String> = emptyList(),
        ): ErrorResponse = ErrorResponse(Error(message, stackTrace))
    }
}
