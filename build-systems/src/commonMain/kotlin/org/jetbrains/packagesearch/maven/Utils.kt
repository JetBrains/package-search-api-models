package org.jetbrains.packagesearch.maven

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML

val ProjectModel.properties
    get() = propertiesContainer?.properties ?: emptyMap()
val ProjectModel.licenses
    get() = licensesContainer?.licenses ?: emptyList()
val ProjectModel.dependencyManagement
    get() = dependencyManagementContainer?.dependencies?.dependencies ?: emptyList()
val ProjectModel.dependencies
    get() = dependenciesContainer?.dependencies ?: emptyList()
val ProjectModel.developers
    get() = developersContainer?.developers ?: emptyList()

fun ProjectModel.copy(
    dependencies: List<Dependency> = this.dependencies,
    dependencyManagement: List<Dependency> = this.dependencyManagement,
    properties: Map<String, String> = this.properties
) = copy (
    dependenciesContainer = Dependencies(dependencies),
    dependencyManagementContainer = DependencyManagement(Dependencies(dependencyManagement)),
    propertiesContainer = Properties(properties)
)

const val POM_XML_NAMESPACE = "http://maven.apache.org/POM/4.0.0"

val MAVEN_CENTRAL_MIRRORS = listOf(
    Url("https://maven-central.storage-download.googleapis.com/maven2"),
    Url("https://repo1.maven.org/maven2")
)

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

internal suspend fun HttpResponse.bodyAsPom(xml: XML) =
    runCatching { body<ProjectModel>() }
        .getOrElse { xml.decodeFromString<ProjectModel>(bodyAsText()) }