package org.jetbrains.packagesearch.api.v3

import korlibs.crypto.SHA256
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.packageversionutils.normalization.NormalizedVersion

/**
 * The base interface for all packages stored in Package Search.
 * When serialized in JSON they will have the field `type`.
 * Available packages types are:
 * - [ApiMavenPackage]s - `type`: "maven"
 *
 */
@Serializable
sealed interface ApiPackage {

    val id: String
    val idHash: String
    val rankingMetric: Double?
    val versions: VersionsContainer<out ApiPackageVersion>

    companion object {
        fun hashPackageId(id: String) =
            SHA256.create()
                .update(id.encodeToByteArray())
                .digest()
                .hex
    }
}

@Serializable
sealed interface ApiPackageVersion {
    val normalized: NormalizedVersion
    val repositoryIds: List<String>
    val vulnerability: Vulnerability
}

@Serializable
data class VersionsContainer<T : ApiPackageVersion>(
    val latestStable: T?,
    val latest: T?,
    val all: Map<String, T>,
)

@Serializable
sealed interface ApiMavenVersion : ApiPackageVersion {
    val dependencies: List<Dependency>
    val artifacts: List<ApiArtifact>
    val name: String?
    val description: String?
    val authors: List<Author>
    val scmUrl: String?
    val licenses: Licenses?
}

@Serializable
@SerialName("maven")
data class ApiMavenPackage(
    override val id: String,
    override val idHash: String,
    override val rankingMetric: Double?,
    override val versions: VersionsContainer<ApiMavenVersion>,
    val groupId: String,
    val artifactId: String,
) : ApiPackage {

    @Serializable
    @SerialName("mavenVersion")
    data class MavenVersion(
        override val normalized: NormalizedVersion,
        override val repositoryIds: List<String>,
        override val vulnerability: Vulnerability,
        override val dependencies: List<Dependency>,
        override val artifacts: List<ApiArtifact>,
        override val name: String?,
        override val description: String?,
        override val authors: List<Author>,
        override val scmUrl: String?,
        override val licenses: Licenses?
    ) : ApiMavenVersion

    @Serializable
    @SerialName("gradleVersion")
    data class GradleVersion(
        override val normalized: NormalizedVersion,
        override val repositoryIds: List<String>,
        override val vulnerability: Vulnerability,
        override val dependencies: List<Dependency>,
        override val artifacts: List<ApiArtifact>,
        override val name: String?,
        override val description: String?,
        override val authors: List<Author>,
        override val scmUrl: String?,
        override val licenses: Licenses?,
        val variants: List<ApiVariant>,
        val parentComponent: String? = null
    ) : ApiMavenVersion

    @Serializable
    data class ApiGradleDependency(
        val group: String,
        val module: String,
        val version: String,
    )

    @Serializable
    sealed interface ApiVariant {

        @Serializable
        sealed interface Attribute {

            companion object {
                fun create(name: String, value: String) = when {
                    name == "org.gradle.jvm.version" -> ComparableInteger(value.toInt())
                    name == "or.gradle.libraryelements" && value == "aar" -> ExactMatch("aar", listOf("jar"))
                    else -> ExactMatch(value)
                }
            }

            fun isCompatible(other: Attribute): Boolean

            @Serializable
            @SerialName("exactMatch")
            data class ExactMatch internal constructor(val value: String, val alternativeValues: List<String> = emptyList()) : Attribute {
                override fun isCompatible(other: Attribute) = when (other) {
                    is ComparableInteger -> false
                    is ExactMatch -> (alternativeValues + value).any { it == other.value }
                }
            }

            @Serializable
            @SerialName("comparableInteger")
            data class ComparableInteger internal constructor(val value: Int) : Attribute {
                override fun isCompatible(other: Attribute) = when (other) {
                    is ComparableInteger -> value < other.value
                    is ExactMatch -> false
                }
            }
        }

        val name: String
        val attributes: Map<String, Attribute>

        @Serializable
        data class WithFiles(
            override val name: String,
            override val attributes: Map<String, Attribute>,
            val dependencies: List<ApiGradleDependency>,
            val files: List<File>,
        ) : ApiVariant

        @Serializable
        data class WithAvailableAt(
            override val name: String,
            override val attributes: Map<String, Attribute>,
            @SerialName("available-at") val availableAt: AvailableAt,
        ) : ApiVariant {

            @Serializable
            data class AvailableAt(
                val url: String,
                val group: String,
                val module: String,
                val version: String,
            )
        }

        @Serializable
        data class File(
            val name: String,
            val url: String,
            val size: Long,
            val sha512: String,
            val sha256: String,
            val sha1: String,
            val md5: String,
        )
    }

}

@Serializable
data class ApiArtifact(
    val name: String,
    val md5: String?,
    val sha1: String?,
    val sha256: String?,
    val sha512: String?,
)
