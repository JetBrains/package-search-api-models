package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ApiRepository {

    val id: String
    val lastChecked: Long?
}

@Serializable
@SerialName("maven")
data class ApiMavenRepository(
    override val id: String,
    override val lastChecked: Long?,
    val url: String,
    val alternateUrls: List<String>? = null,
    val friendlyName: String,
    val userFacingUrl: String? = null,
    val packageCount: Int? = null,
    val artifactCount: Int? = null,
    val namedLinks: String? = null,
) : ApiRepository

@Serializable
@SerialName("cocoapods")
object ApiCocoapodsRepository : ApiRepository {

    override val id = "cocoapods"
    override val lastChecked: Long? = null
}

@Serializable
@SerialName("npm")
object ApiNpmRepository : ApiRepository {

    override val id = "npm"
    override val lastChecked: Long? = null
}
