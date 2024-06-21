package org.jetbrains.packagesearch.maven

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import nl.adaptivity.xmlutil.serialization.XML

public class HttpClientMavenPomProvider(
    public val mirrors: List<MavenUrlBuilder>,
    public val httpClient: HttpClient,
    public val xml: XML = PomResolver.defaultXml(),
) : MavenPomProvider, Closeable by httpClient {
    @Deprecated("Use mirrors instead", ReplaceWith("mirrors"))
    public val repositories: List<MavenUrlBuilder>
        get() = mirrors

    public companion object {
        public fun defaultHttpClient(
            xml: XML = PomResolver.defaultXml(),
            configure: HttpClientConfig<*>.() -> Unit = {},
        ): HttpClient =
            HttpClient {
                install(ContentNegotiation) {
                    val converter = KotlinxSerializationConverter(xml)
                    register(ContentType.Application.Xml, converter)
                    register(ContentType.Text.Xml, converter)
                }
                install(HttpRequestRetry) {
                    retryOnExceptionOrServerErrors(10)
                    constantDelay(100, 50, false)
                }
                configure()
            }
    }

    override suspend fun getPom(
        groupId: String,
        artifactId: String,
        version: String,
    ): ProjectObjectModel = mirrors
        .asFlow()
        .map { it.getPom(groupId, artifactId, version) }
        .firstOrNull()
        ?: error("Failed to fetch POM for $groupId:$artifactId:$version")

    override suspend fun getPomFromMultipleRepositories(
        groupId: String,
        artifactId: String,
        version: String,
    ): Flow<ProjectObjectModel> = mirrors
        .asFlow()
        .map { it.getPom(groupId, artifactId, version) }

    override suspend fun getPomByUrl(url: Url): ProjectObjectModel =
        httpClient.get(url).bodyAsPom(xml)

    private suspend fun MavenUrlBuilder.getPom(
        groupId: String,
        artifactId: String,
        version: String,
    ): ProjectObjectModel = httpClient.get(buildPomUrl(groupId, artifactId, version)).bodyAsPom(xml)

    private suspend fun HttpResponse.bodyAsPom(xml: XML) =
        runCatching { body<ProjectObjectModel>() }.getOrNull()
            ?: xml.decodeFromString(POM_XML_NAMESPACE, bodyAsText())
}
