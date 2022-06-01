package com.jetbrains.packagesearch.api.statistics.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiLicenseStats(
    @SerialName("gpl") val gpl: Int,
    @SerialName("apache-2") val apache2: Int,
    @SerialName("mit") val mit: Int,
    @SerialName("bsd") val bsd: Int
)
