package org.jetbrains.packagesearch.maven

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.xmlStreaming

public val MavenProjectObjectModel.properties: Map<String, String>
    get() = propertiesContainer?.properties ?: emptyMap()
public val MavenProjectObjectModel.licenses: List<License>
    get() = licensesContainer?.licenses ?: emptyList()
public val MavenProjectObjectModel.dependencyManagement: List<Dependency>
    get() = dependencyManagementContainer?.dependencies?.dependencies ?: emptyList()
public val MavenProjectObjectModel.dependencies: List<Dependency>
    get() = dependenciesContainer?.dependencies ?: emptyList()
public val MavenProjectObjectModel.developers: List<Developer>
    get() = developersContainer?.developers ?: emptyList()

public val Contributor.properties: Map<String, String>
    get() = properties?.properties ?: emptyMap()

public fun MavenProjectObjectModel.copy(
    groupId: String? = this.groupId,
    artifactId: String? = this.artifactId,
    parent: Parent? = this.parent,
    dependencies: List<Dependency> = this.dependencies,
    dependencyManagement: List<Dependency> = this.dependencyManagement,
    properties: Map<String, String> = this.properties,
    name: String? = this.name,
    description: String? = this.description,
    scm: Scm? = this.scm,
): MavenProjectObjectModel =
    copy(
        groupId = groupId,
        artifactId = artifactId,
        parent = parent,
        dependenciesContainer = Dependencies(dependencies),
        dependencyManagementContainer = DependencyManagement(Dependencies(dependencyManagement)),
        propertiesContainer = Properties(properties),
        name = name,
        description = description,
        scm = scm,
    )

public const val POM_XML_NAMESPACE: String = "http://maven.apache.org/POM/4.0.0"

public interface MavenUrlBuilder {
    public fun buildArtifactUrl(artifactIdentifier: MavenArtifactIdentifier): Url
    public fun buildPackageMetadataUrl(groupId: String, artifactId: String): Url
}

public fun SimpleMavenUrlBuilder(baseUrl: String): SimpleMavenUrlBuilder =
    SimpleMavenUrlBuilder(Url(baseUrl.removeSuffix("/")))

public class SimpleMavenUrlBuilder(
    private val baseUrl: Url,
) : MavenUrlBuilder {

    override fun buildArtifactUrl(artifactIdentifier: MavenArtifactIdentifier): Url =
        buildUrl {
            protocol = baseUrl.protocol
            host = baseUrl.host
            port = baseUrl.port
            val urlSegments = buildList {
                addAll(baseUrl.pathSegments)
                addAll(artifactIdentifier.groupId.split("."))
                add(artifactIdentifier.artifactId)
                add(artifactIdentifier.version)
                add(buildString {
                    append("${artifactIdentifier.artifactId}-${artifactIdentifier.version}")
                    artifactIdentifier.classifier?.let { append("-$it") }
                    artifactIdentifier.extension
                        ?.removePrefix(".")
                        ?.let { append(".$it") }
                })
            }
            pathSegments = urlSegments
        }

    override fun buildPackageMetadataUrl(
        groupId: String,
        artifactId: String,
    ): Url =
        buildUrl {
            protocol = URLProtocol.HTTPS
            host = baseUrl.host
            port = protocol.defaultPort
            pathSegments =
                buildList {
                    addAll(baseUrl.pathSegments)
                    addAll(groupId.split("."))
                    add(artifactId)
                    add("maven-metadata.xml")
                }
        }
}

public fun MavenUrlBuilder.buildPomUrl(
    groupId: String,
    artifactId: String,
    version: String,
): Url = buildArtifactUrl(MavenArtifactIdentifier(groupId, artifactId, version, extension = "pom"))

public fun MavenUrlBuilder.buildGradleMetadataUrl(
    groupId: String,
    artifactId: String,
    version: String,
): Url = buildArtifactUrl(MavenArtifactIdentifier(groupId, artifactId, version, extension = "module"))

public fun MavenUrlBuilder.buildKotlinMetadataUrl(
    groupId: String,
    artifactId: String,
    version: String,
): Url = buildArtifactUrl(MavenArtifactIdentifier(groupId, artifactId, version, extension = "kotlin-tooling-metadata"))

public fun MavenUrlBuilder.buildJarUrl(
    groupId: String,
    artifactId: String,
    version: String,
): Url = buildArtifactUrl(MavenArtifactIdentifier(groupId, artifactId, version, extension = "jar"))

public fun MavenUrlBuilder.buildSourcesJarUrl(
    groupId: String,
    artifactId: String,
    version: String,
): Url = buildArtifactUrl(MavenArtifactIdentifier(groupId, artifactId, version, classifier = "sources", extension = "jar"))

public fun MavenUrlBuilder.buildJavadocJarUrl(
    groupId: String,
    artifactId: String,
    version: String,
): Url = buildArtifactUrl(MavenArtifactIdentifier(groupId, artifactId, version, classifier = "javadoc", extension = "jar"))

public fun buildMavenUrl(
    groupId: String,
    artifactId: String,
    version: String?,
    host: String,
    classifier: String? = null,
    extension: String,
): Url =
    buildUrl {
        protocol = URLProtocol.HTTPS
        this.host = host
        port = protocol.defaultPort
        pathSegments =
            buildList {
                add("maven2")
                addAll(groupId.split("."))
                add(artifactId)
                version?.let { add(it) }
                add("$artifactId-$version")
                classifier?.let { add("-$it") }
                add(".${extension.removePrefix(".")}")
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
internal fun evaluateProjectProperty(
    projectProperty: String,
    modelAccessor: JsonObject,
): String? {
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
        is JsonObject ->
            evaluateProjectProperty(
                projectProperty =
                projectProperty.removePrefix("$property.")
                    .takeIf { it.isNotEmpty() }
                    ?: return null,
                modelAccessor = accessor,
            )

        // For other types of JSON elements (like arrays), return null as they aren't supported.
        else -> null
    }
}

public inline fun <reified T : Any> XML.decodeFromString(
    namespace: String,
    string: String,
): T = decodeFromReader<T>(ManualNamespaceXmlReader(namespace, string))

public class ManualNamespaceXmlReader(
    private val forcedUri: String,
    xmlString: String,
) : XmlReader by xmlStreaming.newReader(xmlString) {
    override fun toString(): String = "ManualNamespaceXmlReader(namespaceURI=\"$namespaceURI\")"
    override val namespaceURI: String
        get() = forcedUri
    override val name: QName
        get() = QName(forcedUri, localName, prefix)
}

public expect fun parseLastModifiedHeader(header: String): Instant?

public val Headers.LastModified: Instant?
    get() = get(HttpHeaders.LastModified)?.let { parseLastModifiedHeader(it) }

public fun String.isValidSha512Hex(): Boolean {
    // Check if the string is 128 characters long
    if (this.length != 128) {
        return false
    }

    // Check if all characters are valid hexadecimal digits
    return all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
}

public fun String.isValidSha256Hex(): Boolean {
    // Check if the string is 64 characters long
    if (this.length != 64) {
        return false
    }

    // Check if all characters are valid hexadecimal digits
    return all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
}

public fun String.isValidSha1Hex(): Boolean {
    // Check if the string is 40 characters long
    if (this.length != 40) {
        return false
    }

    // Check if all characters are valid hexadecimal digits
    return all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
}

public fun String.isValidMd5Hex(): Boolean {
    // Check if the string is 32 characters long
    if (this.length != 32) {
        return false
    }

    // Check if all characters are valid hexadecimal digits
    return all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
}

internal fun ByteReadChannel.asFlow(): Flow<ByteArray> = flow {
    val buffer = ByteArray(4096)
    while (true) {
        val bytesRead = readAvailable(buffer, 0, buffer.size)
        if (bytesRead <= 0) break
        emit(buffer.copyOf(bytesRead))
    }
}