package org.jetbrains.packagesearch.maven

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("metadata")
public data class MavenMetadata(
    @XmlElement val groupId: String? = null,
    @XmlElement val artifactId: String? = null,
    @XmlElement val version: String? = null,
    @XmlElement val versioning: Versioning? = null
)

@Serializable
@XmlSerialName("versioning")
public data class Versioning(
    @XmlElement val latest: String? = null,
    @XmlElement val release: String? = null,
    val versions: Versions? = null
)

@Serializable
@XmlSerialName("versions")
public data class Versions(
    val version: List<Version>? = null
)

@Serializable
@XmlSerialName("version")
public data class Version(
    @XmlValue val content: String
)