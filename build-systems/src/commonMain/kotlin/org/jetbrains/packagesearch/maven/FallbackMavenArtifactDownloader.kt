package org.jetbrains.packagesearch.maven

public class FallbackMavenArtifactDownloader(
    private var providers: List<MavenArtifactDownloader>,
) : MavenArtifactDownloader {
    override suspend fun getArtifact(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        extension: String,
    ): ProjectObjectModel? {
        for (provider in providers) {
            provider.getPom(groupId, artifactId, version,)?.let { return it }
        }
        return null
    }

    override fun getArtifactSource(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        extension: String
    ): String {
        TODO("Not yet implemented")
    }
}
