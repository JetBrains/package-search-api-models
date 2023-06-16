package org.jetbrains.packagesearch.api.v1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiStandardPackage(
    @SerialName("groupId") override val groupId: String,
    @SerialName("artifactId") override val artifactId: String,
    @SerialName("packaging") val packaging: String? = null,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("latestVersion") val latestVersion: ApiStandardVersion,
    @SerialName("versions") override val versions: List<ApiStandardVersion>,
    @SerialName("dependencyRating") val dependencyRating: Double,
) : ApiPackage<ApiStandardPackage.ApiStandardVersion> {

    @Serializable
    data class ApiStandardVersion(
        @SerialName("version") val version: String,
        @SerialName("stable") val stable: Boolean,
    )
}
