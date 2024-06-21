package org.jetbrains.packagesearch.maven

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow

public fun interface MavenPomProvider {
    public suspend fun getPom(
        groupId: String,
        artifactId: String,
        version: String,
    ): ProjectObjectModel?

    @Deprecated(
        message = "Use getPom instead",
        replaceWith = ReplaceWith("getPomFromMultipleRepositories(groupId, artifactId, version)"),
    )
    public suspend fun getPomFromMultipleRepositories(
        groupId: String,
        artifactId: String,
        version: String,
    ): Flow<ProjectObjectModel> = error("Should not be used")

    @Deprecated(
        message = "Use getPom instead",
        replaceWith = ReplaceWith("getPomByUrl(url)"),
    )
    public suspend fun getPomByUrl(url: Url): ProjectObjectModel? = error("Should not be used")
}
