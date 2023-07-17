package org.jetbrains.packagesearch.maven.central

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MavenCentralApiResponse(
    public val responseHeader: ResponseHeader,
    public val response: Response
)

@Serializable
public data class ResponseHeader(
    public val status: Int,
    @SerialName("QTime") public val qTime: Int,
    public val params: Params
)

@Serializable
public data class Params(
    public val q: String,
    public val core: String,
    public val indent: String,
    public val fl: String,
    public val start: String,
    public val sort: String,
    public val rows: String,
    public val wt: String,
    public val version: String
)

@Serializable
public data class Response(
    public val numFound: Int,
    public val start: Int,
    public val docs: List<Doc>
)

@Serializable
public data class Doc(
    public val id: String,
    public val g: String,
    public val a: String,
    public val v: String,
    public val p: String,
    public val timestamp: Long,
    public val ec: List<String>,
    public val tags: List<String>
)
