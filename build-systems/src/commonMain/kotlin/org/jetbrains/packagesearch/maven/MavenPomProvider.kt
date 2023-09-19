package org.jetbrains.packagesearch.maven

import io.ktor.http.*
import kotlinx.coroutines.flow.Flow

public interface MavenPomProvider {
    public suspend fun getPom(groupId: String, artifactId: String, version: String): ProjectObjectModel
    public suspend fun getPomFromMultipleRepositories(groupId: String, artifactId: String, version: String): Flow<ProjectObjectModel>
    public suspend fun getPomByUrl(url: Url): ProjectObjectModel
}

