package org.jetbrains.packagesearch.api.v4.storage

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v4.core.ScmId

@Serializable
@SerialName("github")
public data class GitHubRepository(
    override val id: ScmId.GitHub,
    override val lastUpdated: Instant,
    override val url: String,
    val createdAt: Instant,
    val topics: Set<String> = emptySet(),
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