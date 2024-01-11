package org.jetbrains.packagesearch.maven

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName


@Serializable
@XmlSerialName(
    value = "project",
    namespace = POM_XML_NAMESPACE,
)
public data class ProjectObjectModel(
    @XmlElement public val modelVersion: String? = null,
    @XmlElement public val groupId: String? = null,
    @XmlElement public val artifactId: String? = null,
    @XmlElement public val version: String? = null,
    @XmlElement public val name: String? = null,
    @XmlElement public val description: String? = null,
    @XmlElement public val url: String? = null,
    public val organization: Organization? = null,
    public val parent: Parent? = null,
    @XmlElement public val packaging: String? = null,
    public val propertiesContainer: Properties? = null,
    public val dependenciesContainer: Dependencies? = null,
    public val dependencyManagementContainer: DependencyManagement? = null,
    public val licensesContainer: Licenses? = null,
    public val developersContainer: Developers? = null,
    public val contributorsContainer: Contributors? = null,
    public val scm: Scm? = null,
    public val issueManagement: IssueManagement? = null
)

@Serializable
@XmlSerialName(
    value = "contributors",
    namespace = POM_XML_NAMESPACE,
)
public data class Contributors(
    @XmlElement public val contributor: List<Contributor> = emptyList()
)

@Serializable
@XmlSerialName(
    value = "contributor",
    namespace = POM_XML_NAMESPACE,
)
public data class Contributor(
    @XmlElement public val name: String? = null,
    @XmlElement public val email: String? = null,
    @XmlElement public val url: String? = null,
    @XmlElement public val organization: String? = null,
    @XmlElement public val organizationUrl: String? = null,
    public val rolesContainer: Roles? = null,
    @XmlElement val timezone: String? = null,
    public val propertiesContainer: Properties? = null
)

@Serializable(with = MavenPomPropertiesXmlSerializer::class)
@XmlSerialName(
    value = "properties",
    namespace = POM_XML_NAMESPACE,
)
public data class Properties(
    public val properties: Map<String, String> = emptyMap()
)

@Serializable
@XmlSerialName(
    value = "issueManagement",
    namespace = POM_XML_NAMESPACE,
)
public data class IssueManagement(
    @XmlElement public val system: String? = null,
    @XmlElement public val url: String? = null
)

@Serializable
@XmlSerialName(
    value = "organization",
    namespace = POM_XML_NAMESPACE,
)
public data class Organization(
    @XmlElement public val name: String? = null,
    @XmlElement public val url: String? = null
)

@Serializable
@XmlSerialName(
    value = "parent",
    namespace = POM_XML_NAMESPACE,
)
public data class Parent(
    @XmlElement public val groupId: String,
    @XmlElement public val artifactId: String,
    @XmlElement public val version: String,
    @XmlElement public val relativePath: String? = null
)

@Serializable
@XmlSerialName(
    value = "dependencies",
    namespace = POM_XML_NAMESPACE,
)
public data class Dependencies(
    @XmlElement public val dependencies: List<Dependency> = emptyList()
)

@Serializable
@XmlSerialName(
    value = "dependency",
    namespace = POM_XML_NAMESPACE,
)
public data class Dependency(
    @XmlElement public val groupId: String,
    @XmlElement public val artifactId: String,
    @XmlElement public val version: String? = null,
    @XmlElement public val classifier: String? = null,
    @XmlElement public val type: String? = null,
    @XmlElement public val scope: String? = null,
    @XmlElement public val optional: Boolean? = null,
    @XmlElement public val systemPath: String? = null,
    public val exclusionsContainer: Exclusions? = null,
)

@Serializable
@XmlSerialName(
    value = "exclusions",
    namespace = POM_XML_NAMESPACE,
)
public data class Exclusions(public val exclusions: List<Exclusion> = emptyList())

@Serializable
@XmlSerialName(
    value = "exclusion",
    namespace = POM_XML_NAMESPACE,
)
public data class Exclusion(
    @XmlElement public val groupId: String? = null,
    @XmlElement public val artifactId: String? = null
)

@Serializable
@XmlSerialName(
    value = "dependencyManagement",
    namespace = POM_XML_NAMESPACE,
)
public data class DependencyManagement(
    public val dependencies: Dependencies? = null
)

@Serializable
@XmlSerialName(
    value = "licenses",
    namespace = POM_XML_NAMESPACE,
)
public data class Licenses(
    public val licenses: List<License>
)

@Serializable
@XmlSerialName(
    value = "license",
    namespace = POM_XML_NAMESPACE,
)
public data class License(
    @XmlElement public val name: String? = null,
    @XmlElement public val url: String? = null,
    @XmlElement public val distribution: String? = null,
    @XmlElement public val comments: String? = null
)

@Serializable
@XmlSerialName(
    value = "developers",
    namespace = POM_XML_NAMESPACE,
)
public data class Developers(
    public val developers: List<Developer>
)

@Serializable
@XmlSerialName(
    value = "developer",
    namespace = POM_XML_NAMESPACE,
)
public data class Developer(
    @XmlElement public val id: String? = null,
    @XmlElement public val name: String? = null,
    @XmlElement public val email: String? = null,
    @XmlElement public val organization: String? = null,
    @XmlElement public val organizationUrl: String? = null,
    @XmlElement public val roles: Roles? = null,
)

@Serializable
@XmlSerialName(
    value = "roles",
    namespace = POM_XML_NAMESPACE,
)
public data class Roles(
    @XmlElement public val roles: List<String>
)

@Serializable
@XmlSerialName(
    value = "scm",
    namespace = POM_XML_NAMESPACE,
)
public data class Scm(
    @XmlElement public val connection: String? = null,
    @XmlElement public val developerConnection: String? = null,
    @XmlElement public val url: String? = null,
    @XmlElement public val tag: String? = null
)
