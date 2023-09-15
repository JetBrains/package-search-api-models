package org.jetbrains.packagesearch.maven.central

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MavenCentralApiResponse(
    public val response: Response,
)

@Serializable
public data class Response(
    public val numFound: Int,
    public val start: Int,
    public val docs: List<Doc>,
)

@Serializable
public data class Doc(
    public val id: String,
    @SerialName("g") public val groupId: String,
    @SerialName("a") public val artifactId: String,
    @SerialName("v") public val version: String? = null,
    @SerialName("p") public val packaging: String,
    public val latestVersion: String? = null,
    public val repositoryId: String? = null,
    public val timestamp: Long,
    public val versionCount: Int? = null,
    public val text: List<String> = emptyList(),
    public val ec: List<String> = emptyList(),
    public val tags: List<String> = emptyList(),
)

@Serializable
public data class Spellcheck(
    val suggestions: List<String> = emptyList(),
)