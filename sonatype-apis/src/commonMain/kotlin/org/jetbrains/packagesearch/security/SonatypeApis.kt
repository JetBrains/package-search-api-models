package org.jetbrains.packagesearch.security

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import kotlinx.serialization.Serializable

@Serializable
data class SonatypeVulnerabilityResponse(
    val coordinates: String,
    val description: String,
    val reference: String,
    val vulnerabilities: List<Vulnerability>,
    val sonatypeOssiScore: Double
)

@Serializable
data class Vulnerability(
    val id: String,
    val displayName: String,
    val title: String,
    val description: String,
    val cvssScore: Double,
    val cvssVector: String,
    val cwe: String,
    val cve: String,
    val reference: String,
    val externalReferences: List<String>
)

@Serializable
data class VulnerabilityRequest(
    val coordinates: List<String>
)

class SonatypeSecurityApiClient(
    private val httpClient: HttpClient = defaultClient()
) : Closeable by httpClient {

    companion object {
        fun defaultClient(credentials: suspend () -> BasicAuthCredentials? = { null }) =
            HttpClient {
                install(ContentNegotiation) {
                    json()
                }
                install(Auth) {
                    basic {
                        credentials(credentials)
                    }
                }
            }
    }

    suspend fun getVulnerabilities(
        request: VulnerabilityRequest,
        authUrl: Boolean = false
    ): List<SonatypeVulnerabilityResponse> {
        val url = buildUrl {
            protocol = URLProtocol.HTTPS
            host = "ossindex.sonatype.org"
            pathSegments = buildList {
                addAll("api", "v3")
                if (authUrl) add("authorized")
                add("component-report")
            }
        }
        val httpResponse = httpClient.post(url) {
            setBody(request)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
        return httpResponse.body<List<SonatypeVulnerabilityResponse>>()
    }

}

private fun <E> MutableList<E>.addAll(vararg elements: E) = elements.forEach(::add)

private fun buildUrl(action: URLBuilder.() -> Unit) = URLBuilder().apply(action).build()

suspend fun SonatypeSecurityApiClient.getVulnerabilities(coordinates: List<String>) =
    getVulnerabilities(VulnerabilityRequest(coordinates))

suspend fun SonatypeSecurityApiClient.getVulnerabilities(requests: List<SonatypeVulnerabilityCoordinate>) =
    getVulnerabilities(requests.map { it.coordinates })

val SonatypeVulnerabilityCoordinate.coordinates
    get() = "$type:$repositoryType/$name@$version"

sealed interface SonatypeVulnerabilityCoordinate {

    val type: String
    val repositoryType: String
    val name: String
    val version: String

    data class MavenPackage(
        val groupId: String,
        val artifactId: String,
        override val version: String,
    ) : SonatypeVulnerabilityCoordinate {
        override val type: String
            get() = "pkg"
        override val repositoryType: String
            get() = "maven"
        override val name: String
            get() = "$groupId/$artifactId"
    }

    data class CocoapodsPackage(
        override val name: String,
        override val version: String
    ) : SonatypeVulnerabilityCoordinate {
        override val type: String
            get() = "pkg"
        override val repositoryType: String
            get() = "cocoapods"
    }

    data class NpnPackage(
        override val name: String,
        override val version: String
    ) : SonatypeVulnerabilityCoordinate {
        override val type: String
            get() = "pkg"
        override val repositoryType: String
            get() = "npm"
    }
}
