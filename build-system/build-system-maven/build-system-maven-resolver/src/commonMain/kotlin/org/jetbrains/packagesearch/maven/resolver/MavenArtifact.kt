package org.jetbrains.packagesearch.maven.resolver

import io.ktor.utils.io.ByteReadChannel
import org.jetbrains.packagesearch.maven.MavenArtifactInfo

public class MavenArtifact(
    public val info: MavenArtifactInfo,
    public val content: ByteReadChannel,
)

