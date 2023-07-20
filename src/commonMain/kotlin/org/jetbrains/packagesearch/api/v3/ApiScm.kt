package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface ApiScm {
    public val scmUrl: String
    public val description: String?
    public val ossHealthIndex: Double?
    public val readmeUrl: String?
}

@Serializable
@SerialName("github")
public data class GitHub(
    public override val scmUrl: String,
    public override val description: String? = null,
    public override val readmeUrl: String? = null,
    public val httpUrl: String,
    public val isFork: Boolean? = null,
    public override val ossHealthIndex: Double?,
    public val stars: Int? = null,
    public val watchers: Int? = null,
    public val forks: Int? = null,
    public val subscribers: Int? = null,
) : ApiScm
