package org.jetbrains.packagesearch.maven

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

public class FallbackMavenPomProvider(
    private var providers: List<MavenPomProvider>,
) : MavenPomProvider {
    override suspend fun getPom(
        groupId: String,
        artifactId: String,
        version: String,
    ): ProjectObjectModel? {
        for (provider in providers) {
            provider.getPom(groupId, artifactId, version)?.let { return it }
        }
        return null
    }
}
