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
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.utils.io.core.Closeable
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.serialization.XML

public class HttpClientMavenPomProvider(
    public val urlBuilder: MavenUrlBuilder,
    public val httpClient: HttpClient,
    public val xml: XML = PomResolver.defaultXml(),
) : MavenPomProvider, Closeable by httpClient {

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
    ): ProjectObjectModel = httpClient.get(urlBuilder.buildPomUrl(groupId, artifactId, version)).bodyAsPom(xml)

    private suspend fun HttpResponse.bodyAsPom(xml: XML) =
        runCatching { body<ProjectObjectModel>() }.getOrNull()
            ?: xml.decodeFromString(ProjectObjectModel.serializer(), bodyAsText(), QName(POM_XML_NAMESPACE))

}
