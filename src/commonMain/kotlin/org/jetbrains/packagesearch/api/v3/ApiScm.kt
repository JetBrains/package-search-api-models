package org.jetbrains.packagesearch.api.v3

import korlibs.crypto.SHA256
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface ApiScm {
    public val url: String
    public val urlHash: String // for ElasticSearch, can't use URL as doc ID via http call
    public val description: String?
    public val ossHealthIndex: Double?

    @Deprecated("Use `readmeUrl` instead", ReplaceWith("readme.rawUrl"))
    public val readmeUrl: String?

    public val readme: Readme?
    public val license: ScmLicenseFile?

    public companion object {
        public fun hashScmUrl(url: String): String =
            SHA256.create()
                .update(url.encodeToByteArray())
                .digest()
                .hex
    }
}

@Deprecated("Use ApiGitHub", ReplaceWith("ApiGitHub"))
public typealias GitHub = ApiGitHub

@Serializable
public data class Readme(
    val rawUrl: String,
    val htmlUrl: String
)

@Serializable
@SerialName("github")
public data class ApiGitHub(
    public override val url: String,
    public override val urlHash: String,
    public override val description: String? = null,
    @Deprecated("Use `readmeUrl` instead", replaceWith = ReplaceWith("readme.rawUrl"))
    public override val readmeUrl: String? = null,
    public override val license: ScmLicenseFile? = null,
    public val htmlUrl: String,
    public val isFork: Boolean? = null,
    public override val ossHealthIndex: Double?,
    public val stars: Int? = null,
    public val watchers: Int? = null,
    public val forks: Int? = null,
    public val subscribers: Int? = null,
    override val readme: Readme? = null,
) : ApiScm
