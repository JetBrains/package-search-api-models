package org.jetbrains.packagesearch.maven

import io.ktor.http.URLProtocol
import io.ktor.http.Url

val ProjectObjectModel.properties
    get() = propertiesContainer?.properties ?: emptyMap()
val ProjectObjectModel.licenses
    get() = licensesContainer?.licenses ?: emptyList()
val ProjectObjectModel.dependencyManagement
    get() = dependencyManagementContainer?.dependencies?.dependencies ?: emptyList()
val ProjectObjectModel.dependencies
    get() = dependenciesContainer?.dependencies ?: emptyList()
val ProjectObjectModel.developers
    get() = developersContainer?.developers ?: emptyList()

fun ProjectObjectModel.copy(
    dependencies: List<Dependency> = this.dependencies,
    dependencyManagement: List<Dependency> = this.dependencyManagement,
    properties: Map<String, String> = this.properties
) = copy (
    dependenciesContainer = Dependencies(dependencies),
    dependencyManagementContainer = DependencyManagement(Dependencies(dependencyManagement)),
    propertiesContainer = Properties(properties)
)

const val POM_XML_NAMESPACE = "http://maven.apache.org/POM/4.0.0"

interface MavenUrlBuilder {
    fun buildArtifactUrl(groupId: String, artifactId: String, version: String, artifactExtension: String): Url
}

object GoogleMavenCentralMirror : MavenUrlBuilder {
    override fun buildArtifactUrl(
        groupId: String,
        artifactId: String,
        version: String,
        artifactExtension: String
    ) = buildUrl {
        protocol = URLProtocol.HTTPS
        host = "maven-central.storage-download.googleapis.com"
        port = protocol.defaultPort
        pathSegments = buildList {
            add("maven2")
            addAll(groupId.split("."))
            add(artifactId)
            add(version)
            add("$artifactId-$version$artifactExtension")
        }
    }
}

internal data class DependencyKey(val groupId: String, val artifactId: String)

internal fun evaluateProjectProperty(projectProperty: String, modelAccessor: StringAccessor.ObjectAccessor): String? {
    val property = projectProperty.split('.').firstOrNull() ?: return null
    val accessor = modelAccessor[property] ?: return null
    return when (accessor) {
        is StringAccessor.SimpleAccessor -> accessor.value
        is StringAccessor.ObjectAccessor -> evaluateProjectProperty(
            projectProperty = projectProperty.removePrefix("$property.")
                .takeIf { it.isNotEmpty() }
                ?: return null,
            modelAccessor = accessor
        )
    }
}

internal expect fun getenv(it: String): String?
internal expect fun getSystemProp(it: String): String?
