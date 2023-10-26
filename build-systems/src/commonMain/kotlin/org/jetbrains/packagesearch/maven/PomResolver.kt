package org.jetbrains.packagesearch.maven

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.adaptivity.xmlutil.serialization.XML

/**
 * PomResolver is a class that resolves Maven POMs (Project Object Models) using a provided MavenPomProvider.
 * It implements the Closeable interface.
 *
 * @property pomProvider The MavenPomProvider used to retrieve the POM.
 * @property xml The XML object used for encoding and decoding POMs. It has a default value if not provided.
 */
public class PomResolver(
    public val pomProvider: MavenPomProvider,
    public val xml: XML = defaultXml()
) : Closeable {

    public companion object {

        /**
         * Generates a default XML configuration for the PomResolver class.
         *
         * @return the default XML configuration
         */
        public fun defaultXml(): XML = XML {
            defaultPolicy {
                ignoreUnknownChildren()
            }
        }

        /**
         * Returns the default MavenPomProvider instance with the provided optional parameters.
         *
         * @param repositories the list of MavenUrlBuilder instances representing the repositories to search for POM files (default: [GoogleMavenCentralMirror])
         * @param xml the XML configuration (default: defaultXml())
         * @param httpClient the HttpClient instance used to make HTTP requests (default: defaultHttpClient(xml))
         * @return the default MavenPomProvider instance
         */
        public fun defaultPomProvider(
            repositories: List<MavenUrlBuilder> = listOf(GoogleMavenCentralMirror),
            xml: XML = defaultXml(),
            httpClient: HttpClient = HttpClientMavenPomProvider.defaultHttpClient(xml)
        ): MavenPomProvider {
            return HttpClientMavenPomProvider(
                repositories,
                httpClient,
                xml
            )
        }

        /**
         * Regular expression used for pattern matching and extraction.
         * This regex pattern is used to match and extract substrings in the format `${...}` from a string.
         * It is used in the `String.resolve()` function to resolve property references.
         *
         * Regex pattern: `\$\{(.*?)\}`
         */
        private val PROPERTY_REFERENCE_REGEX = Regex("""\$\{(.+?)}""")
        private const val UNRESOLVED = "UNRESOLVED"
    }

    /**
     * Retrieves the Project Object Model (POM) for the specified groupId, artifactId, and version.
     *
     * @param groupId The groupId of the project.
     * @param artifactId The artifactId of the project.
     * @param version The version of the project.
     * @return The retrieved ProjectObjectModel or null if it doesn't exist.
     */
    public suspend fun getPom(groupId: String, artifactId: String, version: String): ProjectObjectModel? =
        pomProvider.getPomFromMultipleRepositories(groupId, artifactId, version)
            .firstOrNull()?.let { resolve(it) }

    /**
     * Retrieves the Project Object Model (POM) for the specified parent.
     *
     * @param parent The Parent object containing the groupId, artifactId, version, and optional relativePath.
     * @return The retrieved ProjectObjectModel or null if it doesn't exist.
     */
    private suspend fun getPom(parent: Parent) =
        getPom(parent.groupId, parent.artifactId, parent.version)

    /**
     * Resolves the Project Object Model (POM) using the provided POM text.
     *
     * @param pomText The POM text to be resolved.
     * @return The resolved ProjectObjectModel.
     */
    public suspend fun resolve(pomText: String): ProjectObjectModel =
        resolve(xml.decodeFromString<ProjectObjectModel>(POM_XML_NAMESPACE, pomText))


    /**
     * Resolves the provided Project Object Model (POM) by merging it with its parent POMs and resolving the property values.
     *
     * @param model The Project Object Model to be resolved.
     * @return The resolved Project Object Model.
     */
    public suspend fun resolve(model: ProjectObjectModel): ProjectObjectModel {
        // Initialize the mergedPom with the given model.
        var mergedPom = model

        // Start by retrieving the parent of the given model.
        var currentParent = mergedPom.parent

        // Initialize a count variable to ensure that the loop does not run indefinitely.
        var count = 0

        // Loop to merge parent POMs until a max of 10 parent POMs or until there's no parent.
        while (count < 10) {
            count++

            // If there's no parent, break out of the loop.
            val parent = currentParent ?: break

            // Attempt to retrieve the POM for the parent. If it fails or if it's null, break out of the loop.
            val parentPom = runCatching { getPom(parent) }.getOrNull() ?: break

            // Merge the current mergedPom with its parent. This includes merging the dependencies,
            // dependencyManagement, and properties.
            // Note: The 'distinct()' function ensures that there are no duplicate items.
            mergedPom = mergedPom.copy(
                groupId = mergedPom.groupId ?: parentPom.groupId,
                artifactId = mergedPom.artifactId ?: parentPom.artifactId,
                dependencies = parentPom.dependencies.plus(mergedPom.dependencies).distinct(),
                dependencyManagement = parentPom.dependencyManagement.plus(mergedPom.dependencyManagement).distinct(),
                properties = parentPom.properties + mergedPom.properties,
            )

            // Update the currentParent for the next iteration.
            currentParent = parentPom.parent
        }

        // Convert the mergedPom to a JSON object for easier property value resolution.
        val accessor = Json.encodeToJsonElement(mergedPom).jsonObject

        // Resolve the versions in the dependencyManagement section of the mergedPom.
        // This creates a map with keys as DependencyKey and values as the corresponding dependency with resolved version.
        val resolvedDependencyManagement = mergedPom.dependencyManagement
            .map { it.copy(version = it.version?.resolve(mergedPom.properties, accessor)) }
            .associateBy { DependencyKey(it.groupId, it.artifactId) }

        // Resolve the versions in the dependencies section of the mergedPom.
        // If the version is present in resolvedDependencyManagement, use that. Otherwise, resolve it using properties.
        val resolvedDependencies = mergedPom.dependencies
            .map {
                it.copy(
                    version = resolvedDependencyManagement[DependencyKey(it.groupId, it.artifactId)]
                        ?.takeIf { it.version != null }
                        ?.version
                        ?: it.version?.resolve(mergedPom.properties, accessor)
                )
            }

        // Return the final mergedPom with all resolved properties, dependencies, and dependencyManagement.
        return mergedPom.copy(
            groupId = mergedPom.groupId?.resolve(mergedPom.properties, accessor),
            artifactId = mergedPom.artifactId?.resolve(mergedPom.properties, accessor),
            dependencies = resolvedDependencies,
            dependencyManagement = resolvedDependencyManagement.values.toList(),
            properties = mergedPom.properties.mapValues {
                it.value.resolve(mergedPom.properties, accessor) ?: it.value
            },
            name = mergedPom.name?.resolve(mergedPom.properties, accessor),
            description = mergedPom.description?.resolve(mergedPom.properties, accessor),
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
     * @return The resolved string or null if it contains an unresolved placeholder.
     */
    private fun String.resolve(
        allProperties: Map<String, String?>,
        modelAccessor: JsonObject,
        currentDepth: Int = 0,
    ): String? = replaceProperty {
        when {
            // If the recursion depth is greater than 10, just return the property key
            // as we might be in a potential infinite loop.
            currentDepth > 10 -> it

            // If the property starts with "project.", it's referring to a field within the project's model.
            it.startsWith("project.") -> {
                val res = evaluateProjectProperty(
                    projectProperty = it.removePrefix("project."),
                    modelAccessor = modelAccessor
                ) ?: UNRESOLVED
                res
            }

            // If the property starts with "settings.", it cannot be resolved with the current setup.
            // Return UNRESOLVED.
            it.startsWith("settings.") -> UNRESOLVED

            // If the property starts with "env.", it refers to an environment variable.
            // Retrieve the environment variable value or return UNRESOLVED if not found.
            it.startsWith("env.") -> getenv(it.removePrefix("env.")) ?: UNRESOLVED

            // Default case:
            // 1. Try to resolve the property recursively if present in allProperties.
            // 2. If not found, check for a system property with that key.
            // 3. If still not found, return UNRESOLVED.
            else -> allProperties[it]?.resolve(allProperties, modelAccessor, currentDepth + 1)
                ?: getSystemProp(it)
                ?: UNRESOLVED
        }
    }

    /**
     * This helper function uses [PROPERTY_REFERENCE_REGEX]
     * to identify and replace placeholders in the string with their corresponding values.
     * If the result has any unresolved placeholders (indicated by the presence of UNRESOLVED),
     * the function returns null.
     */
    private inline fun String.replaceProperty(noinline transform: (String) -> CharSequence): String? =
        replace(PROPERTY_REFERENCE_REGEX) { transform(it.groupValues[1]) }
            .takeIf { UNRESOLVED !in it }


    override fun close() {
        if (pomProvider is Closeable) pomProvider.close()
    }
}

internal fun buildUrl(action: URLBuilder.() -> Unit) = URLBuilder().apply(action).build()

public fun MavenUrlBuilder.buildGradleMetadataUrl(groupId: String, artifactId: String, version: String): Url =
    buildArtifactUrl(groupId, artifactId, version, ".module")
