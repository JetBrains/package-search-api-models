package org.jetbrains.packagesearch.maven

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import nl.adaptivity.xmlutil.serialization.XML

public class HttpClientMavenPomProvider(
    public val repositories: List<MavenUrlBuilder>,
    public val httpClient: HttpClient,
    public val xml: XML = PomResolver.defaultXml()
): MavenPomProvider, Closeable by httpClient {

    public companion object {
        public fun defaultHttpClient(xml: XML): HttpClient = HttpClient {
            install(ContentNegotiation) {
                val converter = KotlinxSerializationConverter(xml)
                register(ContentType.Application.Xml, converter)
                register(ContentType.Text.Xml, converter)
            }
            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(10)
                constantDelay(100, 50, false)
            }
        }
    }

    override suspend fun getPom(groupId: String, artifactId: String, version: String): ProjectObjectModel {
        return getPomFromMultipleRepositories(groupId, artifactId, version).first()
    }

    override suspend fun getPomFromMultipleRepositories(
        groupId: String,
        artifactId: String,
        version: String
    ): Flow<ProjectObjectModel> {
        return repositories.asFlow().map {
            getPom(it, groupId, artifactId, version)
        }
    }

    override suspend fun getPomByUrl(url: Url): ProjectObjectModel {
        return httpClient.get(url).bodyAsPom(xml)
    }

    private suspend fun getPom(repository: MavenUrlBuilder, groupId: String, artifactId: String, version: String): ProjectObjectModel {
        val url = repository.buildPomUrl(groupId, artifactId, version)
        return getPomByUrl(url)
    }

    private suspend fun HttpResponse.bodyAsPom(xml: XML) =
        runCatching { body<ProjectObjectModel>() }.getOrNull()
            ?: xml.decodePomFromString(bodyAsText())

    private fun MavenUrlBuilder.buildPomUrl(groupId: String, artifactId: String, version: String): Url =
        buildArtifactUrl(groupId, artifactId, version, ".pom")

}