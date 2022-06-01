package com.jetbrains.packagesearch.api.statistics

import com.jetbrains.packagesearch.api.statistics.entries.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiStatisticsEntry(
    @SerialName("unique_groups") val uniqueGroups: Int,
    @SerialName("unique_artifacts") val uniqueArtifacts: Int,
    @SerialName("unique_versions") val uniqueVersions: Int,
    @SerialName("pom_license_stats") val pomLicenseStats: List<ApiPomLicenseStat>,
    @SerialName("scm_stats") val scmStats: ApiScmStats,
    @SerialName("code_hosting_platform") val codeHostingPlatform: ApiCodeHostingPlatform,
    @SerialName("packaging_stats") val packagingStats: Map<String, Int>,
    @SerialName("github_stats") val githubStats: ApiGithubStats,
    @SerialName("package_stats") val packageStats: ApiPackageStats,
    @SerialName("versions_stats") val versionsStats: ApiVersionStats,
)
