package org.jetbrains.packagesearch.maven

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("metadata", "", "")
public data class MavenPackageMetadata(
    @XmlElement(true) val groupId: String,
    @XmlElement(true) val artifactId: String,
    @XmlElement(true) val version: String? = null,
    @XmlSerialName("versioning", "", "")
    val versioning: Versioning? = null
) {

    public companion object {
        public val defaultXML: XML = XML {
            defaultPolicy {
                pedantic = true
                ignoreUnknownChildren()
            }
        }
        public fun fromXml(string: String): MavenPackageMetadata = defaultXML.decodeFromString(string)

    }

    @Serializable
    public data class Versioning(
        @XmlElement(true)
        val latest: String? = null,
        @XmlElement(true)
        val release: String? = null,
        @XmlChildrenName("version")
        val versions: List<String>,
        @XmlElement(true)
        @Serializable(with = MavenDateTimeSerializer::class)
        val lastUpdated: Instant? = null
    )
}

