package org.jetbrains.packagesearch.maven

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML

class PomResolver(
    val repositories: List<Url> = MAVEN_CENTRAL_MIRRORS,
    val xml: XML = defaultXml(),
    val httpClient: HttpClient = defaultHttpClient(xml),
) {

    suspend fun resolve(groupId: String, artifactId: String, version: String): ProjectObjectModel {
        val model = repositories.asFlow()
            .map { buildPomUrl(it, groupId, artifactId, version) }
            .map { httpClient.get(it).bodyAsPom(xml) }
            .catch { it.printStackTrace() }
            .firstOrNull()
            ?: error(
                "Pom not found for $groupId:$artifactId:$version in:" +
                        "\n${repositories.joinToString("\n") { "- $it" }}"
            )

        return resolve(model)
    }

    suspend fun resolve(url: Url) = resolve(httpClient.get(url).bodyAsPom(xml))

    suspend fun resolve(pomText: String): ProjectObjectModel =
        resolve(xml.decodeFromString<ProjectObjectModel>(pomText))

    suspend fun resolve(model: ProjectObjectModel): ProjectObjectModel {
        val pomHierarchy = buildList {
            add(model)
            var currentParent = model.parent
            while (currentParent != null) {
                runCatching { resolve(currentParent!!) }.getOrNull()
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
            properties = mergedPom.properties.mapValues { it.value.resolve(mergedPom.properties, accessor) ?: it.value }
        )
    }

    private fun buildUrl(action: URLBuilder.() -> Unit) = URLBuilder().apply(action).build()

    private fun buildPomUrl(from: Url, groupId: String, artifactId: String, version: String) = buildUrl {
        protocol = from.protocol
        host = from.host
        port = from.port
        pathSegments = buildList {
            addAll(from.pathSegments)
            addAll(groupId.split('.'))
            add(artifactId)
            add(version)
            add("$artifactId-$version.pom")
        }
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

private fun defaultXml() = XML {
    defaultPolicy {
        ignoreUnknownChildren()
    }
}

private fun defaultHttpClient(xml: XML) = HttpClient {
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