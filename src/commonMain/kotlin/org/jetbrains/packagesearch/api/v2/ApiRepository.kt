package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiRepository(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("type") val type: String,
    @SerialName("alternate_urls") val alternateUrls: List<String>? = null,
    @SerialName("friendly_name") val friendlyName: String,
    @SerialName("user_facing_url") val userFacingUrl: String? = null,
    @SerialName("package_count") val packageCount: Int? = null,
    @SerialName("artifact_count") val artifactCount: Int? = null,
    @SerialName("named_links") val namedLinks: ApiNamedLinks? = null,
    @SerialName("last_checked") val lastChecked: Long? = null,
) {

    companion object {
        const val TYPE_MAVEN = "maven"
    }

    @Serializable
    data class ApiNamedLinks(
        @SerialName("browsable_url_template") val browsableUrlTemplate: String,
    )
}
