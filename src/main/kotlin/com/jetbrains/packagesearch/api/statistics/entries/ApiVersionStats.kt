package com.jetbrains.packagesearch.api.statistics.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiVersionStats(
    @SerialName("is_kotlin_multiplatform") val isKotlinMultiplatform: Int,
    @SerialName("has_classifier") val hasClassifier: Int,
)
