package org.jetbrains.packagesearch.maven

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow

public fun interface MavenPomProvider {
    public suspend fun getPom(
        groupId: String,
        artifactId: String,
        version: String,
    ): ProjectObjectModel?
}
