package com.jetbrains.packagesearch.api.statistics.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiGithubStats(
    @SerialName("has_readme") val hasReadme: Int,
    @SerialName("has_license") val hasLicense: Int,
    @SerialName("license_stats") val licenseStats: Map<String, Int>
)
