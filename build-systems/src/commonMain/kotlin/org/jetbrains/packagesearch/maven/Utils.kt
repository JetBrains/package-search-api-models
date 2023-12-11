package org.jetbrains.packagesearch.maven

import io.ktor.http.URLProtocol
import io.ktor.http.Url
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.serialization.XML

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
    groupId: String? = this.groupId,
    artifactId: String? = this.artifactId,
    parent: Parent? = this.parent,
    dependencies: List<Dependency> = this.dependencies,
    dependencyManagement: List<Dependency> = this.dependencyManagement,
    properties: Map<String, String> = this.properties,
    name: String? = this.name,
    description: String? = this.description,
): ProjectObjectModel = copy (
    groupId = groupId,
    artifactId = artifactId,
    parent = parent,
    dependenciesContainer = Dependencies(dependencies),
    dependencyManagementContainer = DependencyManagement(Dependencies(dependencyManagement)),
    propertiesContainer = Properties(properties),
    name = name,
    description = description
)

public const val POM_XML_NAMESPACE: String = "http://maven.apache.org/POM/4.0.0"

public interface MavenUrlBuilder {
    public fun buildArtifactUrl(groupId: String, artifactId: String, version: String, artifactExtension: String): Url
    public fun buildMetadataUrl(groupId: String, artifactId: String): Url
}

public class SimpleMavenUrlBuilder(
    rawBaseUrl: String,
): MavenUrlBuilder {
    private val baseUrl = rawBaseUrl.removePrefix("https://")
    override fun buildArtifactUrl(
        groupId: String,
        artifactId: String,
        version: String,
        artifactExtension: String
    ): Url = buildUrl {
        protocol = URLProtocol.HTTPS
        host = baseUrl
        port = protocol.defaultPort
        pathSegments = buildList {
            add("maven2")
            addAll(groupId.split("."))
            add(artifactId)
            add(version)
            add("$artifactId-$version$artifactExtension")
        }
    }

    override fun buildMetadataUrl(groupId: String, artifactId: String): Url =
        buildUrl {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            port = protocol.defaultPort
            pathSegments = buildList {
                add("maven2")
                addAll(groupId.split("."))
                add(artifactId)
                add("maven-metadata.xml")
            }
        }
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

    override fun buildMetadataUrl(groupId: String, artifactId: String): Url = buildUrl {
        protocol = URLProtocol.HTTPS
        host = "maven-central.storage-download.googleapis.com"
        port = protocol.defaultPort
        pathSegments = buildList {
            add("maven2")
            addAll(groupId.split("."))
            add(artifactId)
            add("maven-metadata.xml")
        }
    }
}

internal data class DependencyKey(val groupId: String, val artifactId: String)

/**
 * Evaluates the value of the provided project property within the given model accessor.
 *
 * @param projectProperty The project property to evaluate.
 * @param modelAccessor The JSON object representing the model accessor.
 * @return The evaluated value of the project property, or null if it cannot be evaluated.
 */
internal fun evaluateProjectProperty(projectProperty: String, modelAccessor: JsonObject): String? {
    // Split the given project property string based on '.' and retrieve the first part.
    // If it's null or doesn't exist, return null.
    val property = projectProperty.split('.').firstOrNull() ?: return null

    // Use the extracted property to access the corresponding value from the modelAccessor.
    // If the property isn't found in the modelAccessor, return null.
    val accessor = modelAccessor[property] ?: return null

    return when (accessor) {
        // If the accessed value is a primitive (like a string or number), return its content directly.
        is JsonPrimitive -> accessor.content

        // If the accessed value is another JsonObject, it indicates the property might have more nested parts.
        // Recursively evaluate this nested property by removing the currently accessed part from the property string.
        is JsonObject -> evaluateProjectProperty(
            projectProperty = projectProperty.removePrefix("$property.")
                .takeIf { it.isNotEmpty() }
                ?: return null,
            modelAccessor = accessor
        )

        // For other types of JSON elements (like arrays), return null as they aren't supported.
        else -> null
    }
}

@Deprecated(
    "Use decodeFromString instead",
    ReplaceWith(
        "decodeFromString<ProjectObjectModel>(POM_XML_NAMESPACE, string)",
        "org.jetbrains.packagesearch.maven.decodeFromString",
        "org.jetbrains.packagesearch.maven.ProjectObjectModel"
    )
)
public fun XML.decodePomFromString(string: String): ProjectObjectModel {
    return decodeFromString(POM_XML_NAMESPACE, string)
}

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
public inline fun <reified T : Any> XML.decodeFromString(namespace: String, string: String): T {
    return decodeFromReader<T>(object : XmlReader by XmlStreaming.newReader(string) {
        override val namespaceURI: String get() = namespace
    })
}
