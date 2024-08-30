package org.jetbrains.packagesearch.maven

public interface MavenArtifactDownloader {
    public suspend fun getArtifact(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String? = null,
        extension: String,
    ): ProjectObjectModel?

    public fun getArtifactSource(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String? = null,
        extension: String,
    ): String
}

public suspend fun MavenArtifactDownloader.getPom(
    groupId: String,
    artifactId: String,
    version: String,
): ProjectObjectModel? = getArtifact(groupId, artifactId, version, extension = "pom")

public suspend fun MavenArtifactDownloader.getJar(
    groupId: String,
    artifactId: String,
    version: String,
): ProjectObjectModel? = getArtifact(groupId, artifactId, version, extension = "jar")

public suspend fun MavenArtifactDownloader.getSourcesJar(
    groupId: String,
    artifactId: String,
    version: String,
): ProjectObjectModel? = getArtifact(groupId, artifactId, version, classifier = "sources", extension = "jar")

public suspend fun MavenArtifactDownloader.getJavadocJar(
    groupId: String,
    artifactId: String,
    version: String,
): ProjectObjectModel? = getArtifact(groupId, artifactId, version, classifier = "javadoc", extension = "jar")

public fun MavenArtifactDownloader.getGradleMetadata(
    groupId: String,
    artifactId: String,
    version: String,
): String = getArtifactSource(groupId, artifactId, version, extension = "module")

public fun MavenArtifactDownloader.getKotlinMetadata(
    groupId: String,
    artifactId: String,
    version: String,
): String = getArtifactSource(groupId, artifactId, version, classifier = "kotlin-tooling-metadata", extension = "json")