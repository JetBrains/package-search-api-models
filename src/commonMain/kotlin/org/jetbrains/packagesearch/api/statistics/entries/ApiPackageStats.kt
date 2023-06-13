package org.jetbrains.packagesearch.api.statistics.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiPackageStats(
    @SerialName("has_pom_license") val hasPomLicense: Int,
    @SerialName("has_authors") val hasAuthors: Int,
    @SerialName("has_stackoverflow") val hasStackOverflow: Int,
    @SerialName("has_github_info") val hasGithubInfo: Int
)
