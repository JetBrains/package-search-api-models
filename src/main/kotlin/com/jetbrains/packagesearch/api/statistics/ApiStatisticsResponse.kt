package com.jetbrains.packagesearch.api.statistics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiStatisticsResponse(
    @SerialName("global") val global: ApiStatisticsEntry,
    @SerialName("repositories") val repositories: Map<String, ApiStatisticsEntry>
)
