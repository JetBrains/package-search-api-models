package org.jetbrains.packagesearch.maven

import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

public class FallbackMavenPomProvider(
    private var providers: List<MavenPomProvider>
) : MavenPomProvider {
    override suspend fun getPom(groupId: String, artifactId: String, version: String): ProjectObjectModel? {
        for (provider in providers) {
            provider.getPom(groupId, artifactId, version)?.let { return it }
        }
        return null
    }

    override suspend fun getPomFromMultipleRepositories(
        groupId: String,
        artifactId: String,
        version: String
    ): Flow<ProjectObjectModel> {
        return providers.firstOrNull()?.getPomFromMultipleRepositories(groupId, artifactId, version) ?: emptyFlow()
    }

    override suspend fun getPomByUrl(url: Url): ProjectObjectModel? {
        for (provider in providers) {
            provider.getPomByUrl(url)?.let { return it }
        }
        return null
    }
}