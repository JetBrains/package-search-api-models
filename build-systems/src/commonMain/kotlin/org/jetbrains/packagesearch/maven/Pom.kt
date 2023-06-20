package org.jetbrains.packagesearch.maven

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*


@Serializable
@XmlSerialName(
    value = "project",
    namespace = POM_XML_NAMESPACE,
)
data class ProjectObjectModel(
    @XmlElement val modelVersion: String? = null,
    @XmlElement val groupId: String? = null,
    @XmlElement val artifactId: String? = null,
    @XmlElement val version: String? = null,
    @XmlElement val name: String? = null,
    @XmlElement val description: String? = null,
    @XmlElement val url: String? = null,
    val organization: Organization? = null,
    val parent: Parent? = null,
    @XmlElement val packaging: String? = null,
    val propertiesContainer: Properties? = null,
    val dependenciesContainer: Dependencies? = null,
    val dependencyManagementContainer: DependencyManagement? = null,
    val licensesContainer: Licenses? = null,
    val developersContainer: Developers? = null,
    val contributorsContainer: Contributors? = null,
    val scm: Scm? = null,
    val issueManagement: IssueManagement? = null
)

@Serializable
@XmlSerialName(
    value = "contributors",
    namespace = POM_XML_NAMESPACE,
)
data class Contributors(
    @XmlElement val contributor: List<Contributor> = emptyList()
)

@Serializable
@XmlSerialName(
    value = "contributor",
    namespace = POM_XML_NAMESPACE,
)
data class Contributor(
    @XmlElement val name: String? = null,
    @XmlElement val email: String? = null,
    @XmlElement val url: String? = null,
    @XmlElement val organization: String? = null,
    @XmlElement val organizationUrl: String? = null,
    private val rolesContainer: Roles? = null,
    @XmlElement val timezone: String? = null,
    private val propertiesContainer: Properties? = null
) {
    val roles
        get() = rolesContainer?.roles ?: emptyList()

    val properties
        get() = propertiesContainer?.properties ?: emptyMap()

}

@Serializable(with = MavenPomPropertiesXmlSerializer::class)
@XmlSerialName(
    value = "properties",
    namespace = POM_XML_NAMESPACE,
)
data class Properties(
    val properties: Map<String, String> = emptyMap()
)

@Serializable
@XmlSerialName(
    value = "issueManagement",
    namespace = POM_XML_NAMESPACE,
)
data class IssueManagement(
    @XmlElement val system: String? = null,
    @XmlElement val url: String? = null
)

@Serializable
@XmlSerialName(
    value = "organization",
    namespace = POM_XML_NAMESPACE,
)
data class Organization(
    @XmlElement val name: String? = null,
    @XmlElement val url: String? = null
)

@Serializable
@XmlSerialName(
    value = "parent",
    namespace = POM_XML_NAMESPACE,
)
data class Parent(
    @XmlElement val groupId: String,
    @XmlElement val artifactId: String,
    @XmlElement val version: String,
    @XmlElement val relativePath: String? = null
)

@Serializable
@XmlSerialName(
    value = "dependencies",
    namespace = POM_XML_NAMESPACE,
)
data class Dependencies(
    @XmlElement val dependencies: List<Dependency> = emptyList()
)

@Serializable
@XmlSerialName(
    value = "dependency",
    namespace = POM_XML_NAMESPACE,
)
data class Dependency(
    @XmlElement val groupId: String,
    @XmlElement val artifactId: String,
    @XmlElement val version: String? = null,
    @XmlElement val classifier: String? = null,
    @XmlElement val type: String? = null,
    @XmlElement val scope: String? = null,
    @XmlElement val optional: Boolean? = null,
    @XmlElement val systemPath: String? = null,
    val exclusionsContainer: Exclusions? = null,
)

@Serializable
@XmlSerialName(
    value = "exclusions",
    namespace = POM_XML_NAMESPACE,
)
data class Exclusions(val exclusions: List<Exclusion> = emptyList())

@Serializable
@XmlSerialName(
    value = "exclusion",
    namespace = POM_XML_NAMESPACE,
)
data class Exclusion(
    @XmlElement val groupId: String? = null,
    @XmlElement val artifactId: String? = null
)

@Serializable
@XmlSerialName(
    value = "dependencyManagement",
    namespace = POM_XML_NAMESPACE,
)
data class DependencyManagement(
    val dependencies: Dependencies
)

@Serializable
@XmlSerialName(
    value = "licenses",
    namespace = POM_XML_NAMESPACE,
)
data class Licenses(
    val licenses: List<License>
)

@Serializable
@XmlSerialName(
    value = "license",
    namespace = POM_XML_NAMESPACE,
)
data class License(
    @XmlElement val name: String? = null,
    @XmlElement val url: String? = null,
    @XmlElement val distribution: String? = null,
    @XmlElement val comments: String? = null
)

@Serializable
@XmlSerialName(
    value = "developers",
    namespace = POM_XML_NAMESPACE,
)
data class Developers(
    val developers: List<Developer>
)

@Serializable
@XmlSerialName(
    value = "developer",
    namespace = POM_XML_NAMESPACE,
)
data class Developer(
    @XmlElement val id: String? = null,
    @XmlElement val name: String? = null,
    @XmlElement val email: String? = null,
    @XmlElement val organization: String? = null,
    @XmlElement val roles: Roles? = null
)

@Serializable
@XmlSerialName(
    value = "roles",
    namespace = POM_XML_NAMESPACE,
)
data class Roles(
    @XmlElement val roles: List<String>
)

@Serializable
@XmlSerialName(
    value = "scm",
    namespace = POM_XML_NAMESPACE,
)
data class Scm(
    @XmlElement val connection: String? = null,
    @XmlElement val developerConnection: String? = null,
    @XmlElement val url: String? = null,
    @XmlElement val tag: String? = null
)
