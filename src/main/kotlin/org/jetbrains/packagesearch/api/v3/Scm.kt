package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Scm {

    val scmUrl: String
    val description: String?
    @SerialName("oss_health_index") val ossHealthIndex: Double?
    @SerialName("readme_url") val readmeUrl: String?
}

@Serializable
@SerialName("github")
data class GitHub(
    override val scmUrl: String,
    override val description: String? = null,
    @SerialName("readme_url") override val readmeUrl: String? = null,
    val httpUrl: String,
    @SerialName("is_fork") val isFork: Boolean? = null,
    @SerialName("oss_health_index") override val ossHealthIndex: Double?,
    val stars: Int? = null,
    val watchers: Int? = null,
    val forks: Int? = null,
    val subscribers: Int? = null
) : Scm
