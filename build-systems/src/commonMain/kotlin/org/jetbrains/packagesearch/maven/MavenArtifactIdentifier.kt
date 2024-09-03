package org.jetbrains.packagesearch.maven

import kotlinx.serialization.Serializable

@Serializable
public data class MavenArtifactIdentifier(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val classifier: String? = null,
    val extension: String? = null,
) {

    val fileName: String
        get() = buildString {
            append(artifactId)
            append("-")
            append(version)
            classifier?.let { append("-$it") }
            extension?.let { append(".$it") }
        }

    override fun toString(): String = fileName
}