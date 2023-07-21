package org.jetbrains.packagesearch.maven

import io.ktor.http.URLProtocol
import io.ktor.http.Url

public val ProjectObjectModel.properties: Map<String, String>
    get() = propertiesContainer?.properties ?: emptyMap()
public val ProjectObjectModel.licenses: List<License>
    get() = licensesContainer?.licenses ?: emptyList()
public val ProjectObjectModel.dependencyManagement: List<Dependency>
    get() = dependencyManagementContainer?.dependencies?.dependencies ?: emptyList()
public val ProjectObjectModel.dependencies: List<Dependency>
    get() = dependenciesContainer?.dependencies ?: emptyList()
public val ProjectObjectModel.developers: List<Developer>
    get() = developersContainer?.developers ?: emptyList()

public val Contributor.properties: Map<String, String>
    get() = propertiesContainer?.properties ?: emptyMap()

public fun ProjectObjectModel.copy(
    dependencies: List<Dependency> = this.dependencies,
    dependencyManagement: List<Dependency> = this.dependencyManagement,
    properties: Map<String, String> = this.properties,
    name: String? = this.name,
    description: String? = this.description,
): ProjectObjectModel = copy (
    dependenciesContainer = Dependencies(dependencies),
    dependencyManagementContainer = DependencyManagement(Dependencies(dependencyManagement)),
    propertiesContainer = Properties(properties),
    name = name,
    description = description
)

public const val POM_XML_NAMESPACE: String = "http://maven.apache.org/POM/4.0.0"

public interface MavenUrlBuilder {
    public fun buildArtifactUrl(groupId: String, artifactId: String, version: String, artifactExtension: String): Url
}

public object GoogleMavenCentralMirror : MavenUrlBuilder {
    public override fun buildArtifactUrl(
        groupId: String,
        artifactId: String,
        version: String,
        artifactExtension: String
    ): Url = buildUrl {
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
