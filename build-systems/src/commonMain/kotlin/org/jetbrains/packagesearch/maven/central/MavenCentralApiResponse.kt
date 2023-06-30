package org.jetbrains.packagesearch.maven.central

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MavenCentralApiResponse(
    val responseHeader: ResponseHeader,
    val response: Response
)

@Serializable
data class ResponseHeader(
    val status: Int,
    @SerialName("QTime") val qTime: Int,
    val params: Params
)

@Serializable
data class Params(
    val q: String,
    val core: String,
    val indent: String,
    val fl: String,
    val start: String,
    val sort: String,
    val rows: String,
    val wt: String,
    val version: String
)

@Serializable
data class Response(
    val numFound: Int,
    val start: Int,
    val docs: List<Doc>
)

@Serializable
data class Doc(
    val id: String,
    val g: String,
    val a: String,
    val v: String,
    val p: String,
    val timestamp: Long,
    val ec: List<String>,
    val tags: List<String>
)
