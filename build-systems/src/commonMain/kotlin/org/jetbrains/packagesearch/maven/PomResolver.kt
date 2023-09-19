package org.jetbrains.packagesearch.maven

import io.ktor.client.*
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.adaptivity.xmlutil.serialization.XML

public class PomResolver(
    public val pomProvider: MavenPomProvider,
    public val xml: XML = defaultXml()
) : Closeable {

    public companion object {

        public fun defaultXml(): XML = XML {
            defaultPolicy {
                ignoreUnknownChildren()
            }
        }

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
    }

    public suspend fun resolve(groupId: String, artifactId: String, version: String): ProjectObjectModel? =
        pomProvider.getPomFromMultipleRepositories(groupId, artifactId, version)
            .firstOrNull()?.let { resolve(it) }

    public suspend fun resolve(url: Url): ProjectObjectModel =
        pomProvider.getPomByUrl(url)

    public suspend fun resolve(pomText: String): ProjectObjectModel =
        resolve(xml.decodePomFromString(pomText))

    public suspend fun resolve(model: ProjectObjectModel): ProjectObjectModel {
        var mergedPom = model
        var count = 0
        while (count < 10) {
            count++
            val parent = mergedPom.parent ?: break
            val parentPom = runCatching { resolve(parent) }.getOrNull() ?: break
            mergedPom = mergedPom.copy(
                dependencies = parentPom.dependencies.plus(mergedPom.dependencies).distinct(),
                dependencyManagement = parentPom.dependencyManagement.plus(mergedPom.dependencyManagement).distinct(),
                properties = parentPom.properties + mergedPom.properties,
            )
        }

        val accessor = Json.encodeToJsonElement(mergedPom).jsonObject

        val resolvedDependencyManagement = mergedPom.dependencyManagement
            .map { it.copy(version = it.version?.resolve(mergedPom.properties, accessor)) }
            .associateBy { DependencyKey(it.groupId, it.artifactId) }
        val resolvedDependencies = mergedPom.dependencies
            .map {
                it.copy(
                    version = resolvedDependencyManagement[DependencyKey(it.groupId, it.artifactId)]
                        ?.takeIf { it.version != null }
                        ?.version
                        ?: it.version?.resolve(mergedPom.properties, accessor)
                )
            }
        return mergedPom.copy(
            dependencies = resolvedDependencies,
            dependencyManagement = resolvedDependencyManagement.values.toList(),
            properties = mergedPom.properties.mapValues {
                it.value.resolve(mergedPom.properties, accessor) ?: it.value
            },
            name = mergedPom.name?.resolve(mergedPom.properties, accessor),
            description = mergedPom.description?.resolve(mergedPom.properties, accessor),
        )
    }

    private suspend fun resolve(parent: Parent) =
        resolve(parent.groupId, parent.artifactId, parent.version)

    private val regex = Regex("""\$\{(.*?)}""")
    private val UNRESOLVED = "UNRESOLVED"

    private fun String.resolve(
        allProperties: Map<String, String?>,
        modelAccessor: JsonObject,
        currentDepth: Int = 0,
    ): String? = replace(regex) {
        when {
            currentDepth > 10 -> it.value
            it.groupValues[1].startsWith("project.") -> {
                val res =
                    evaluateProjectProperty(it.groupValues[1].removePrefix("project."), modelAccessor) ?: UNRESOLVED
                res
            }

            it.groupValues[1].startsWith("settings.") -> UNRESOLVED
            it.groupValues[1].startsWith("env.") -> getenv(it.groupValues[1].removePrefix("env.")) ?: UNRESOLVED
            else -> allProperties[it.groupValues[1]]?.resolve(allProperties, modelAccessor, currentDepth + 1)
                ?: getSystemProp(it.groupValues[1])
                ?: UNRESOLVED
        }
    }.takeIf { UNRESOLVED !in it }

    override fun close() {
        if (pomProvider is Closeable) pomProvider.close()
    }
}

internal fun buildUrl(action: URLBuilder.() -> Unit) = URLBuilder().apply(action).build()

public fun MavenUrlBuilder.buildGradleMetadataUrl(groupId: String, artifactId: String, version: String): Url =
    buildArtifactUrl(groupId, artifactId, version, ".module")
