package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Repository {

    val id: String
    @SerialName("last_checked") val lastChecked: Long?
}

@Serializable
@SerialName("maven")
data class MavenRepository(
    override val id: String,
    override val lastChecked: Long?,
    val url: String,
    @SerialName("alternate_urls") val alternateUrls: List<String>? = null,
    @SerialName("friendly_name") val friendlyName: String,
    @SerialName("user_facing_url") val userFacingUrl: String? = null,
    @SerialName("package_count") val packageCount: Int? = null,
    @SerialName("artifact_count") val artifactCount: Int? = null,
    @SerialName("named_links") val namedLinks: String? = null,
) : Repository

@Serializable
@SerialName("cocoapods")
object Cocoapods : Repository {

    override val id = "cocoapods"
    override val lastChecked: Long? = null
}

@Serializable
@SerialName("npm")
object Npm : Repository {

    override val id = "npm"
    override val lastChecked: Long? = null
}
