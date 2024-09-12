package org.jetbrains.packagesearch.maven

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
public data class MavenArtifactInfo(
    val identifier: MavenArtifactIdentifier,
    val createdAt: Instant? = null,
    val md5: String,
    val sha1: String,
    val sha256: String,
    val sha512: String,
    val size: Long,
)

public val MavenArtifactIdentifier.fileName: String
    get() = buildString {
        append(artifactId)
        append("-")
        append(version)
        classifier?.let { append("-$it") }
        extension?.let { append(".$it") }
    }