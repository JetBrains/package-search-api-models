package org.jetbrains.packagesearch.maven.resolver

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.utils.io.ByteReadChannel
import korlibs.crypto.MD5
import korlibs.crypto.SHA1
import korlibs.crypto.SHA256
import korlibs.crypto.SHA512
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.onEach
import nl.adaptivity.xmlutil.serialization.XML
import org.jetbrains.packagesearch.maven.MavenArtifactIdentifier
import org.jetbrains.packagesearch.maven.MavenArtifactInfo

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

    override suspend fun getArtifactContent(identifier: MavenArtifactIdentifier): Flow<ByteArray>? =
        httpClient
            .get(urlBuilder.buildArtifactUrl(identifier))
            .takeIf { it.status.isSuccess() }
            ?.bodyAsChannel()
            ?.asFlow()

    override suspend fun getArtifact(identifier: MavenArtifactIdentifier): MavenArtifact? = coroutineScope {
        val httpResponse = httpClient
            .get(urlBuilder.buildArtifactUrl(identifier))

        if (!httpResponse.status.isSuccess()) return@coroutineScope null

        val sha512Url = urlBuilder.buildArtifactUrl(identifier.copy(extension = "${identifier.extension}.sha512"))
        val md5Url = urlBuilder.buildArtifactUrl(identifier.copy(extension = "${identifier.extension}.md5"))
        val sha1Url = urlBuilder.buildArtifactUrl(identifier.copy(extension = "${identifier.extension}.sha1"))
        val sha256Url = urlBuilder.buildArtifactUrl(identifier.copy(extension = "${identifier.extension}.sha256"))

        val md5Deferred = async {
            httpClient.get(md5Url)
                .bodyAsText()
                .takeIf { it.isValidMd5Hex() }
        }

        val sha1Deferred = async {
            httpClient.get(sha1Url)
                .bodyAsText()
                .takeIf { it.isValidSha1Hex() }
        }

        val sha256Deferred = async {
            httpClient.get(sha256Url)
                .bodyAsText()
                .takeIf { it.isValidSha256Hex() }
        }

        val sha512Deferred = async {
            httpClient.get(sha512Url)
                .bodyAsText()
                .takeIf { it.isValidSha512Hex() }
        }

        val md5 = md5Deferred.await()
        val sha1 = sha1Deferred.await()
        val sha256 = sha256Deferred.await()
        val sha512 = sha512Deferred.await()
        val contentLength = httpResponse.contentLength()

        if (md5 != null && sha1 != null && sha256 != null && sha512 != null && contentLength != null) {
            return@coroutineScope MavenArtifact(
                info = MavenArtifactInfo(
                    identifier = identifier,
                    createdAt = httpResponse.headers.LastModified,
                    md5 = md5,
                    sha1 = sha1,
                    sha256 = sha256,
                    sha512 = sha512,
                    size = contentLength,
                ),
                content = httpResponse.bodyAsChannel()
            )
        }

        val md5Digest = MD5()
        val sha1Digest = SHA1()
        val sha256Digest = SHA256()
        val sha512Digest = SHA512()
        var size = 0L

        val content = httpResponse
            .bodyAsChannel()
            .asFlow()
            .onEach {
                md5Digest.update(it)
                sha1Digest.update(it)
                sha256Digest.update(it)
                sha512Digest.update(it)
                size += it.size
            }
            .fold(ByteArray(0)) { acc, bytes -> acc + bytes }

        MavenArtifact(
            info = MavenArtifactInfo(
                identifier = identifier,
                createdAt = httpResponse.headers.LastModified,
                md5 = md5Digest.digest().hex,
                sha1 = sha1Digest.digest().hex,
                sha256 = sha256Digest.digest().hex,
                sha512 = sha512Digest.digest().hex,
                size = size,
            ),
            content = ByteReadChannel(content)
        )
    }

    override fun getArtifactSource(identifier: MavenArtifactIdentifier): String =
        urlBuilder.buildArtifactUrl(identifier).toString()

}
