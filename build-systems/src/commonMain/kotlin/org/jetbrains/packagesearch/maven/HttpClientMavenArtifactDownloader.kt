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
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.utils.io.core.Closeable
import nl.adaptivity.xmlutil.serialization.XML

public class HttpClientMavenArtifactDownloader(
    public val urlBuilder: MavenUrlBuilder,
    public val httpClient: HttpClient,
    public val xml: XML = PomResolver.defaultXml(),
) : MavenArtifactDownloader {

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

    override suspend fun getArtifact(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        extension: String,
    ): ProjectObjectModel? = httpClient
        .get(urlBuilder.buildArtifactUrl(groupId, artifactId, version, classifier, extension))
        .takeIf { it.status.isSuccess() }
        ?.bodyAsPom(xml)

    override fun getArtifactSource(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        extension: String
    ): String = urlBuilder.buildArtifactUrl(groupId, artifactId, version, classifier, extension).toString()

    private suspend fun HttpResponse.bodyAsPom(xml: XML) =
        runCatching { body<ProjectObjectModel>() }.getOrNull()
            ?: xml.decodeFromString(POM_XML_NAMESPACE, bodyAsText())

}
