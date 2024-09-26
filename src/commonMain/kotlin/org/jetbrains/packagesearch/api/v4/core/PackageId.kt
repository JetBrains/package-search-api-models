package org.jetbrains.packagesearch.api.v4.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface PackageId {

    public companion object {
        public fun Maven(groupId: String, artifactId: String): Maven.WithoutVersion =
            Maven.WithoutVersion(groupId, artifactId)
        public fun Maven(groupId: String, artifactId: String, version: String): Maven.WithVersion =
            Maven.WithVersion(groupId, artifactId, version)


        public fun parse(string: String): PackageId? {
            val pieces = string.split(":")
            return when {
                pieces.size < 2 -> null
                pieces[0] == "maven" -> when (pieces.size) {
                    3 -> Maven(pieces[1], pieces[2])
                    4 -> Maven(pieces[1], pieces[2], pieces[3])
                    else -> null
                }
                else -> null
            }
        }
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

public val PackageId.Maven.baseId: PackageId.Maven
    get() = when (this){
        is PackageId.Maven.WithoutVersion -> this
        is PackageId.Maven.WithVersion -> PackageId.Maven(groupId, artifactId)
    }

