package com.jetbrains.packagesearch.api.v1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiLegacyResponse(
    @SerialName("items") val items: List<ApiMinimalPackage>
)
