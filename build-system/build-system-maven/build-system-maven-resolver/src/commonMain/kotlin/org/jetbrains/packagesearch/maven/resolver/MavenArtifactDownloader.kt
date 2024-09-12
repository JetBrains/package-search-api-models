package org.jetbrains.packagesearch.maven.resolver

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.jetbrains.packagesearch.maven.MavenArtifactIdentifier

public interface MavenArtifactDownloader {

    public suspend fun getArtifactContent(identifier: MavenArtifactIdentifier): Flow<ByteArray>?
    public suspend fun getArtifact(identifier: MavenArtifactIdentifier): MavenArtifact?
    public fun getArtifactSource(identifier: MavenArtifactIdentifier): String
}

public suspend fun MavenArtifactDownloader.getPom(
    groupId: String,
    artifactId: String,
    version: String,
): MavenArtifact? = getArtifact(MavenArtifactIdentifier(groupId, artifactId, version, extension = "pom"))

public suspend fun MavenArtifactDownloader.getPomContent(
    groupId: String,
    artifactId: String,
    version: String,
): String? = getArtifactContent(MavenArtifactIdentifier(groupId, artifactId, version, extension = "pom"))
    ?.toList()
    ?.joinToString("") { it.decodeToString() }

public suspend fun MavenArtifactDownloader.getJar(
    groupId: String,
    artifactId: String,
    version: String,
): MavenArtifact? = getArtifact(MavenArtifactIdentifier(groupId, artifactId, version, extension = "jar"))

public suspend fun MavenArtifactDownloader.getSourcesJar(
    groupId: String,
    artifactId: String,
    version: String,
): MavenArtifact? =
    getArtifact(MavenArtifactIdentifier(groupId, artifactId, version, classifier = "sources", extension = "jar"))

public suspend fun MavenArtifactDownloader.getJavadocJar(
    groupId: String,
    artifactId: String,
    version: String,
): MavenArtifact? =
    getArtifact(MavenArtifactIdentifier(groupId, artifactId, version, classifier = "javadoc", extension = "jar"))

public suspend fun MavenArtifactDownloader.getGradleMetadata(
    groupId: String,
    artifactId: String,
    version: String,
): MavenArtifact? = getArtifact(MavenArtifactIdentifier(groupId, artifactId, version, extension = "module"))

public suspend fun MavenArtifactDownloader.getKotlinMetadata(
    groupId: String,
    artifactId: String,
    version: String,
): MavenArtifact? = getArtifact(
    MavenArtifactIdentifier(
        groupId = groupId,
        artifactId = artifactId,
        version = version,
        classifier = "kotlin-tooling-metadata",
        extension = "json"
    )
)
