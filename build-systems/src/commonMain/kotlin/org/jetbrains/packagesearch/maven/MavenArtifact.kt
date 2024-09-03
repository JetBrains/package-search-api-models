package org.jetbrains.packagesearch.maven

import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

public class MavenArtifact(
    public val info: Info,
    public val content: ByteReadChannel,
) {

    @Serializable
    public data class Info(
        val identifier: MavenArtifactIdentifier,
        val createdAt: Instant? = null,
        val md5: String,
        val sha1: String,
        val sha256: String,
        val sha512: String,
        val size: Long,
    )
}