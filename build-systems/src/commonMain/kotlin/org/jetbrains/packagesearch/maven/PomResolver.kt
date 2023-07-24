package org.jetbrains.packagesearch.maven

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import nl.adaptivity.xmlutil.serialization.XML

public class PomResolver(
    public val repositories: List<MavenUrlBuilder> = listOf(GoogleMavenCentralMirror),
    public val xml: XML = defaultXml(),
    public val httpClient: HttpClient = defaultHttpClient(xml),
) : Closeable by httpClient {

    public companion object {

        public fun defaultXml(): XML = XML {
            defaultPolicy {
                ignoreUnknownChildren()
            }
        }

        public fun defaultHttpClient(xml: XML): HttpClient = HttpClient {
            install(ContentNegotiation) {
                val converter = KotlinxSerializationConverter(xml)
                register(ContentType.Application.Xml, converter)
                register(ContentType.Text.Xml, converter)
            }
            install(HttpRequestRetry) {
                maxRetries = 5
                constantDelay(100, 50, false)
            }
        }
    }

    public suspend fun resolve(groupId: String, artifactId: String, version: String): ProjectObjectModel? =
        repositories.asFlow()
            .map { it.buildPomUrl(groupId, artifactId, version) }
            .map { httpClient.get(it).bodyAsPom(xml) }
            .firstOrNull()
            ?.let { resolve(it) }

    public suspend fun resolve(url: Url): ProjectObjectModel =
        resolve(httpClient.get(url).body<ProjectObjectModel>())

    public suspend fun resolve(pomText: String): ProjectObjectModel =
        resolve(xml.decodePomFromString(pomText))

    public suspend fun resolve(model: ProjectObjectModel): ProjectObjectModel {
        val pomHierarchy = buildList {
            add(model)
            var currentParent = model.parent
            while (currentParent != null) {
                runCatching { resolve(currentParent!!) }
                    .getOrNull()
                    ?.let {
                        add(it)
                        currentParent = it.parent
                    }
            }
        }

        val mergedPom = pomHierarchy.reduce { acc, projectModel ->
            acc.copy(
                dependencies = acc.dependencies + projectModel.dependencies,
                dependencyManagement = acc.dependencyManagement + projectModel.dependencyManagement,
                properties = acc.properties + projectModel.properties,
            )
        }

        val accessor = mergedPom.asAccessor()

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
        modelAccessor: StringAccessor.ObjectAccessor,
        currentDepth: Int = 0
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
}

private suspend fun HttpResponse.bodyAsPom(xml: XML) =
    runCatching { body<ProjectObjectModel>() }.getOrNull()
        ?: xml.decodePomFromString(bodyAsText())

internal fun buildUrl(action: URLBuilder.() -> Unit) = URLBuilder().apply(action).build()

public fun MavenUrlBuilder.buildPomUrl(groupId: String, artifactId: String, version: String): Url =
    buildArtifactUrl(groupId, artifactId, version, ".pom")

public fun MavenUrlBuilder.buildGradleMetadataUrl(groupId: String, artifactId: String, version: String): Url =
    buildArtifactUrl(groupId, artifactId, version, ".module")
