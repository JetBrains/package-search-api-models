package org.jetbrains.packagesearch.maven.resolver

import io.ktor.http.URLBuilder
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.adaptivity.xmlutil.serialization.XML
import org.jetbrains.packagesearch.maven.MavenArtifactIdentifier
import org.jetbrains.packagesearch.maven.MavenProjectObjectModel
import org.jetbrains.packagesearch.maven.POM_XML_NAMESPACE
import org.jetbrains.packagesearch.maven.Parent


/**
 * PomResolver is a class that resolves Maven POMs (Project Object Models) using a provided MavenPomProvider.
 * It implements the Closeable interface.
 *
 * @property MavenArtifactDownloader The MavenArtifactDownloader used to retrieve the POMs.
 * @property xml The XML object used for encoding and decoding POMs. It has a default value if not provided.
 */
public class PomResolver(
    public val artifactDownloaders: List<MavenArtifactDownloader>,
    public val xml: XML = defaultXml(),
) {

    init {
        require(artifactDownloaders.isNotEmpty()) { "At least one ${MavenArtifactDownloader::class.simpleName} must be provided." }
    }

    public companion object {
        /**
         * Generates a default XML configuration for the PomResolver class.
         *
         * @return the default XML configuration
         */
        public fun defaultXml(): XML =
            XML {
                defaultPolicy {
                    ignoreUnknownChildren()
                }
            }

        /**
         * Regular expression used for pattern matching and extraction.
         * This regex pattern is used to match and extract substrings in the format `${...}` from a string.
         * It is used in the `String.resolve()` function to resolve property references.
         *
         * Regex pattern: `\$\{(.*?)\}`
         */
        private val PROPERTY_REFERENCE_REGEX = Regex("""\$\{(.+?)}""")
    }

    /**
     * Retrieves the Project Object Model (POM) for the specified groupId, artifactId, and version.
     *
     * @param groupId The groupId of the project.
     * @param artifactId The artifactId of the project.
     * @param version The version of the project.
     * @return The retrieved ProjectObjectModel or null if it doesn't exist.
     */
    public suspend fun getPom(
        groupId: String,
        artifactId: String,
        version: String,
    ): MavenProjectObjectModel? = artifactDownloaders
        .asFlow()
        .mapNotNull { it.getPomContent(groupId, artifactId, version) }
        .map { xml.decodeFromString<MavenProjectObjectModel>(POM_XML_NAMESPACE, it) }
        .firstOrNull()

    /**
     * Retrieves the Project Object Model (POM) for the specified parent.
     *
     * @param parent The Parent object containing the groupId, artifactId, version, and optional relativePath.
     * @return The retrieved ProjectObjectModel or null if it doesn't exist.
     */
    private suspend fun getPom(parent: Parent) = getPom(parent.groupId, parent.artifactId, parent.version)

    /**
     * Resolves the Project Object Model (POM) using the provided POM text.
     *
     * @param pomText The POM text to be resolved.
     * @return The resolved ProjectObjectModel.
     */
    public suspend fun resolve(pomText: String): MavenProjectObjectModel =
        resolve(xml.decodeFromString<MavenProjectObjectModel>(POM_XML_NAMESPACE, pomText))

    /**
     * Resolves the Project Object Model (POM) using the provided POM text.
     *
     * @param groupId The groupId of the project.
     * @param artifactId The artifactId of the project.
     * @param version The version of the project.
     * @return The resolved ProjectObjectModel.
     */
    public suspend fun resolve(groupId: String, artifactId: String, version: String): MavenProjectObjectModel =
        resolve(
            getPom(groupId, artifactId, version)
                ?: error(buildString {
                    append("Failed to resolve POM for id `$groupId:$artifactId:$version` in any of the providers:")
                    artifactDownloaders.forEach {
                        val identifier = MavenArtifactIdentifier(groupId, artifactId, version, extension = "pom")
                        appendLine("  - ${it::class.simpleName}: ${it.getArtifactSource(identifier)}")
                    }
                })
        )

    /**
     * Resolves the provided Project Object Model (POM) by merging it with its parent POMs and resolving the property values.
     *
     * @param model The Project Object Model to be resolved.
     * @return The resolved Project Object Model.
     */
    public suspend fun resolve(model: MavenProjectObjectModel): MavenProjectObjectModel {
        // Initialize the mergedPom with the given model.
        var mergedPom = model

        // Start by retrieving the parent of the given model.
        var currentParent = mergedPom.parent

        // Initialize a count variable to ensure that the loop does not run indefinitely.
        var count = 0
        val knowParents = mutableSetOf<MavenId>()

        // Loop to merge parent POMs until a max of 10 parent POMs or until there's no parent.
        while (count < 10) {
            count++

            // If there's no parent, break out of the loop.
            val parent = currentParent ?: break
            if (parent.id in knowParents) break

            // Attempt to retrieve the POM for the parent. If it fails or if it's null, break out of the loop.
            val parentPom = runCatching { getPom(parent) }.getOrNull() ?: break
            knowParents.add(parent.id)

            // Merge the current mergedPom with its parent. This includes merging the dependencies,
            // dependencyManagement, and properties.
            // Note: The 'distinct()' function ensures that there are no duplicate items.
            mergedPom =
                mergedPom.copy(
                    groupId = mergedPom.groupId ?: parentPom.groupId,
                    artifactId = mergedPom.artifactId ?: parentPom.artifactId,
                    dependencies = parentPom.dependencies.plus(mergedPom.dependencies).distinct(),
                    dependencyManagement = parentPom.dependencyManagement.plus(mergedPom.dependencyManagement)
                        .distinct(),
                    properties = parentPom.properties + mergedPom.properties,
                )

            // Update the currentParent for the next iteration.
            currentParent = parentPom.parent
        }

        // Convert the mergedPom to a JSON object for easier property value resolution.
        val accessor = Json.encodeToJsonElement(mergedPom).jsonObject

        // Resolve the versions in the dependencyManagement section of the mergedPom.
        // This creates a map with keys as DependencyKey and values as the corresponding dependency with resolved version.
        val resolvedDependencyManagement =
            mergedPom.dependencyManagement
                .map { it.copy(version = it.version?.resolve(mergedPom.properties, accessor)) }
                .associateBy { DependencyKey(it.groupId, it.artifactId) }

        // Resolve the versions in the dependencies section of the mergedPom.
        // If the version is present in resolvedDependencyManagement, use that. Otherwise, resolve it using properties.
        val resolvedDependencies =
            mergedPom.dependencies
                .map {
                    it.copy(
                        version =
                        resolvedDependencyManagement[DependencyKey(it.groupId, it.artifactId)]
                            ?.takeIf { it.version != null }
                            ?.version
                            ?: it.version?.resolve(mergedPom.properties, accessor),
                    )
                }

        // Return the final mergedPom with all resolved properties, dependencies, and dependencyManagement.
        return mergedPom.copy(
            groupId = mergedPom.groupId?.resolve(mergedPom.properties, accessor),
            artifactId = mergedPom.artifactId?.resolve(mergedPom.properties, accessor),
            dependencies = resolvedDependencies,
            dependencyManagement = resolvedDependencyManagement.values.toList(),
            properties = mergedPom.properties.mapValues { it.value.resolve(mergedPom.properties, accessor) },
            name = mergedPom.name?.resolve(mergedPom.properties, accessor),
            description = mergedPom.description?.resolve(mergedPom.properties, accessor),
            scm =
            mergedPom.scm?.copy(
                connection = mergedPom.scm?.connection?.resolve(mergedPom.properties, accessor),
                developerConnection = mergedPom.scm?.developerConnection?.resolve(mergedPom.properties, accessor),
                url = mergedPom.scm?.url?.resolve(mergedPom.properties, accessor),
                tag = mergedPom.scm?.tag?.resolve(mergedPom.properties, accessor),
            ),
        )
    }

    /**
     * Resolves the provided string by replacing placeholders with their corresponding values.
     *
     * It replaces placeholders within a string
     * (like ${property.key}) with their respective values. It leverages nested property resolution
     * (properties referring to other properties), and provides mechanisms to resolve from the
     * project model, environment variables, and system properties.
     *
     * @param allProperties A map of all properties and their values.
     * @param modelAccessor The JSON object used to access values from the model.
     * @param currentDepth The current depth of recursion.
     * @return The resolved string or the original one if it contains an unresolved placeholder.
     */
    private fun String.resolve(
        allProperties: Map<String, String?>,
        modelAccessor: JsonObject,
        currentDepth: Int = 0,
    ): String =
        replaceProperty {
            when {
                // If the recursion depth is greater than 10, just return null
                currentDepth > 10 -> null

                // If the property starts with "settings.", it cannot be resolved with the current setup.
                it.startsWith("settings.") -> null

                // If the property starts with "env.", it refers to an environment variable.
                // It cannot be resolved with the current setup.
                it.startsWith("env.") -> null

                // If the property starts with "project.", it's referring to a field within the project's model.
                it.startsWith("project.") ->
                    evaluateProjectProperty(
                        projectProperty = it.removePrefix("project."),
                        modelAccessor = modelAccessor,
                    )

                // Default case:
                // Try to resolve the property recursively if present in allProperties.
                else -> allProperties[it]?.resolve(allProperties, modelAccessor, currentDepth + 1)
            }
        }

    /**
     * This helper function uses [PROPERTY_REFERENCE_REGEX]
     * to identify and replace placeholders in the string with their corresponding values.
     * If a placeholder cannot be resolved, the placeholder is returned as is.
     */
    private fun String.replaceProperty(transform: (String) -> CharSequence?): String =
        replace(PROPERTY_REFERENCE_REGEX) { transform(it.groupValues[1]) ?: it.groupValues.first() }

}


private data class MavenId(val groupId: String, val artifactId: String, val version: String)

private val Parent.id
    get() = MavenId(groupId, artifactId, version)

internal fun buildUrl(action: URLBuilder.() -> Unit) = URLBuilder().apply(action).build()
