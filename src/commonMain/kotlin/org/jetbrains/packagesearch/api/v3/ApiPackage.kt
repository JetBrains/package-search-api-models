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
public sealed interface ApiPackage {

    public val id: String
    public val idHash: String
    public val rankingMetric: Double?
    public val versions: VersionsContainer<out ApiPackageVersion>
    public val name: String?
    public val coordinates: String
    public val description: String?
    public val licenses: Licenses?
    public val authors: List<Author>
    public val scm: ApiScm?

    public companion object {
        public fun hashPackageId(id: String): String =
            SHA256.create()
                .update(id.encodeToByteArray())
                .digest()
                .hex
    }
}

@Serializable
public sealed interface ApiPackageVersion {
    public companion object {
        public const val MAVEN_REPO_ID: String = "maven2"
    }
    public val normalized: NormalizedVersion
    public val repositoryIds: List<String>
    public val vulnerability: Vulnerability
}

@Serializable
public data class VersionsContainer<T : ApiPackageVersion>(
    public val latestStable: T? = null,
    public val latest: T,
    public val all: Map<String, T>,
)

@Serializable
public sealed interface ApiMavenVersion : ApiPackageVersion {
    public val dependencies: List<Dependency>
    public val artifacts: List<ApiArtifact>
    public val name: String?
    public val description: String?
    public val authors: List<Author>
    public val scmUrl: String?
    public val licenses: Licenses?
}

@Serializable
@SerialName("maven")
public data class ApiMavenPackage(
    public override val id: String,
    public override val idHash: String,
    public override val rankingMetric: Double? = null,
    public override val versions: VersionsContainer<out ApiMavenVersion>,
    public val groupId: String,
    public val artifactId: String,
    override val scm: ApiScm? = null,
) : ApiPackage {

    override val coordinates: String
        get() = "$groupId:$artifactId"
    public override val name: String?
        get() = versions.latest?.name
    public override val description: String?
        get() = versions.latest?.description
    public override val licenses: Licenses?
        get() = versions.latest?.licenses
    public override val authors: List<Author>
        get() = versions.latest?.authors ?: emptyList()

    @Serializable
    @SerialName("mavenVersion")
    public data class MavenVersion(
        public override val normalized: NormalizedVersion,
        public override val repositoryIds: List<String>,
        public override val vulnerability: Vulnerability,
        public override val dependencies: List<Dependency>,
        public override val artifacts: List<ApiArtifact>,
        public override val name: String? = null,
        public override val description: String? = null,
        public override val authors: List<Author>,
        public override val scmUrl: String?,
        public override val licenses: Licenses? = null,
    ) : ApiMavenVersion

    @Serializable
    @SerialName("gradleVersion")
    public data class GradleVersion(
        public override val normalized: NormalizedVersion,
        public override val repositoryIds: List<String>,
        public override val vulnerability: Vulnerability,
        public override val dependencies: List<Dependency>,
        public override val artifacts: List<ApiArtifact>,
        public override val name: String? = null,
        public override val description: String? = null,
        public override val authors: List<Author>,
        public override val scmUrl: String?,
        public override val licenses: Licenses? = null,
        public val variants: List<ApiVariant>,
        public val parentComponent: String? = null,
    ) : ApiMavenVersion {
        @Serializable
        public sealed interface ApiVariant {

            @Serializable
            public sealed interface Attribute {

                public companion object {
                    public fun create(name: String, value: String): Attribute = when {
                        name == "org.gradle.jvm.version" -> ComparableInteger(value.toInt())
                        else -> ExactMatch(value)
                    }
                }

                public fun isCompatible(other: Attribute): Boolean

                @Serializable
                @SerialName("exactMatch")
                public data class ExactMatch internal constructor(
                    public val value: String,
                ) : Attribute {

                    public override fun isCompatible(other: Attribute): Boolean = when (other) {
                        is ComparableInteger -> false
                        is ExactMatch -> value == other.value
                    }
                }

                @Serializable
                @SerialName("comparableInteger")
                public data class ComparableInteger internal constructor(public val value: Int) : Attribute {
                    public override fun isCompatible(other: Attribute): Boolean = when (other) {
                        is ComparableInteger -> value <= other.value
                        is ExactMatch -> false
                    }
                }
            }

            public val name: String
            public val attributes: Map<String, Attribute>

            @Serializable
            public data class WithFiles(
                public override val name: String,
                public override val attributes: Map<String, Attribute>,
                public val dependencies: List<ApiGradleDependency>,
                public val files: List<File>,
            ) : ApiVariant

            @Serializable
            public data class WithAvailableAt(
                public override val name: String,
                public override val attributes: Map<String, Attribute>,
                @SerialName("available-at") public val availableAt: AvailableAt,
            ) : ApiVariant {

                @Serializable
                public data class AvailableAt(
                    public val url: String,
                    public val group: String,
                    public val module: String,
                    public val version: String,
                )
            }

            @Serializable
            public data class File(
                public val name: String,
                public val url: String,
                public val size: Long,
                public val sha1: String,
                public val md5: String,
                public val sha256: String? = null,
                public val sha512: String? = null,
            )
        }
    }

    @Serializable
    public data class ApiGradleDependency(
        public val group: String,
        public val module: String,
        public val version: String?,
    )
}

@Serializable
public data class ApiArtifact(
    public val name: String,
    public val md5: String? = null,
    public val sha1: String? = null,
    public val sha256: String? = null,
    public val sha512: String? = null,
)
