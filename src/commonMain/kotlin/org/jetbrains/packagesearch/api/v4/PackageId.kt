package org.jetbrains.packagesearch.api.v4

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface PackageId {

    public companion object {
        public fun Maven(groupId: String, artifactId: String): Maven.WithoutVersion =
            Maven.WithoutVersion(groupId, artifactId)
        public fun Maven(groupId: String, artifactId: String, version: String): Maven.WithVersion =
            Maven.WithVersion(groupId, artifactId, version)
    }

    @Serializable
    public sealed interface Maven : PackageId {

        public val groupId: String
        public val artifactId: String

        @Serializable
        @SerialName("maven")
        public data class WithoutVersion(override val groupId: String, override val artifactId: String) : Maven {
            override fun toString(): String = "maven:$groupId:$artifactId"
        }

        @Serializable
        @SerialName("maven_with_version")
        public data class WithVersion(override val groupId: String, override val artifactId: String, val version: String) :
            Maven {
            override fun toString(): String = "maven:$groupId:$artifactId:$version"
        }
    }

}

public val PackageId.Maven.WithVersion.baseId: PackageId.Maven
    get() = PackageId.Maven.WithoutVersion(groupId, artifactId)

