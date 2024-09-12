package org.jetbrains.packagesearch.api.v4

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public sealed interface ScmId {

    public companion object {
        public fun fromUrl(url: String): ScmId? = when {
            "github" in url -> {
                url.substringAfter("github.com/")
                    .removeSuffix(".git")
                    .removeSuffix("/")
                    .split("/")
                    .filterNot { it.isEmpty() }
                    .take(2)
                    .takeIf { it.size == 2 }
                    ?.let { (owner, name) -> GitHub(owner, name) }
            }

            else -> null
        }
    }

    @Serializable
    @SerialName("github")
    public data class GitHub(
        val owner: String,
        val name: String
    ) : ScmId
}

@Serializable
public sealed interface Scm {
    public val id: ScmId

    @Serializable
    public sealed interface Resolved : Scm {
        public val lastUpdated: Instant
        public val url: String
    }

    @Serializable
    @SerialName("unresolved")
    public data class Unresolved(
        override val id: ScmId,
        val foundAt: Instant = Clock.System.now(),
    ) : Scm

    @Serializable
    @SerialName("errored")
    public data class Errored(
        override val id: ScmId,
        val error: JsonElement,
        val erroredAt: Instant = Clock.System.now(),
    ) : Scm

}

@Serializable
@SerialName("github")
public data class GitHubRepository(
    override val id: ScmId.GitHub,
    override val lastUpdated: Instant,
    override val url: String,
    val createdAt: Instant,
    val topics: List<String> = emptyList(),
    val homepageUrl: String? = null,
    val stargazerCount: Int,
    val forkCount: Int,
    val license: LicenseInfo? = null,
    val openIssuesCount: Int,
    val closedIssuesCount: Int,
    val openedPullRequestsCount: Int,
    val closedPullRequestsCount: Int,
    val owner: Owner,
    val releases: List<Release> = emptyList(),
) : Scm.Resolved {

    @Serializable
    public data class LicenseInfo(
        val url: String? = null,
        val name: String,
        val nickname: String? = null,
        val spdxId: String? = null
    )

    @Serializable
    public data class Release(
        val name: String? = null,
        val tagName: String,
        val createdAt: Instant,
        val description: String? = null
    )

    @Serializable
    public data class Owner(
        val login: String,
        val avatarUrl: String,
        val url: String
    )
}

