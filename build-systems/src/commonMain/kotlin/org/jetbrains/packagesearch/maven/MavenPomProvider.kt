package org.jetbrains.packagesearch.maven

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow

public interface MavenPomProvider {
    public val httpClient: HttpClient
    public suspend fun getPom(groupId: String, artifactId: String, version: String): ProjectObjectModel
    public suspend fun getPomFromMultipleRepositories(groupId: String, artifactId: String, version: String): Flow<ProjectObjectModel>
    public suspend fun getPomByUrl(url: Url): ProjectObjectModel
}

