package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiInstallAnalytics(
    @SerialName("dependency") val dependency: ApiAnalyticsDependency,
    @SerialName("target") val target: ApiAnalyticsTarget? = null,
    @SerialName("source") val source: ApiAnalyticsSource,
) {
    @Serializable
    data class ApiAnalyticsDependency(
        @SerialName("coordinates") val coordinates: ApiAnalyticsCoordinates,
        @SerialName("repository_ids") val repositoryIds: List<String>,
    ) {
        @Serializable
        data class ApiAnalyticsCoordinates(
            @SerialName("group_id") val groupId: String,
            @SerialName("artifact_id") val artifactId: String,
            @SerialName("version") val version: String,
            @SerialName("classifier") val classifier: String? = null,
            @SerialName("packaging") val packaging: String? = null,
        )
    }

    @Serializable
    data class ApiAnalyticsTarget(
        @SerialName("build_system") val buildSystem: String,
        @SerialName("scopes") val scopes: List<String>,
        @SerialName("installed_repository_ids") val installedRepositoryIds: List<String>,
        @SerialName("detected_features") val detectedFeatures: List<String>? = null,
    )

    @Serializable
    data class ApiAnalyticsSource(
        @SerialName("ide") val ide: ApiAnalyticsIde,
        @SerialName("plugin") val plugin: ApiAnalyticsPlugin,
        @SerialName("request_id") val requestId: String? = null,
        @SerialName("cdn_request_id") val cdnRequestId: String? = null,
    ) {
        @Serializable
        data class ApiAnalyticsIde(
            @SerialName("product") val product: String,
            @SerialName("version") val version: String,
        )

        @Serializable
        data class ApiAnalyticsPlugin(
            @SerialName("version") val version: String,
        )
    }
}
