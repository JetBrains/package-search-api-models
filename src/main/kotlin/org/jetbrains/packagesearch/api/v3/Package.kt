package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The base interface for all packages stored in Package Search.
 * When serialized in JSON they will have the field `type`.
 * Available packages types are:
 * - [GradlePackage]s - `type`: "gradle"
 * - [MavenPackage]s - `type`: "maven"
 *
 */
@Serializable
sealed interface Package {

    val id: String
    val description: String?
    val authors: List<Author>
    val scmUrl: String?
    val licenses: Licenses?
    @SerialName("ranking_metric") val rankingMetric: Double?
}

@Serializable
data class Vulnerability(
    @SerialName("is_vulnerable") val isVulnerable: Boolean,
    val issues: List<String> = emptyList()
)

@Serializable
sealed interface Version {
    @SerialName("version_string") val versionString: String
    @SerialName("last_changed") val lastChanged: Long
    val stable: Boolean
    @SerialName("repository_ids") val repositoryIds: List<String>
    val vulnerability: Vulnerability
}

@Serializable
data class VersionContainer<T : Version>(
    val latest: T,
    val all: List<T>
)

@Serializable
@SerialName("maven")
data class MavenPackage(
    override val id: String,
    override val description: String?,
    override val authors: List<Author>,
    @SerialName("repository_urls") override val scmUrl: String?,
    override val licenses: Licenses?,
    @SerialName("ranking_metric") override val rankingMetric: Double?,
    val versions: VersionContainer<MavenVersion>,
    val group: String,
    val artifactId: String
) : Package {

    @Serializable
    @SerialName("maven_version")
    data class MavenVersion(
        @SerialName("version_string") override val versionString: String,
        @SerialName("last_changed") override val lastChanged: Long,
        override val stable: Boolean,
        @SerialName("repository_ids") override val repositoryIds: List<String>,
        val dependencies: List<Dependency>,
        override val vulnerability: Vulnerability
    ) : Version
}

@Serializable
@SerialName("gradle")
data class GradlePackage(
    override val id: String,
    override val description: String?,
    override val authors: List<Author>,
    @SerialName("repository_urls") override val scmUrl: String?,
    override val licenses: Licenses?,
    @SerialName("ranking_metric") override val rankingMetric: Double?,
    val versions: VersionContainer<GradleVersion>,
    val group: String,
    val module: String,
) : Package {

    @Serializable
    @SerialName("gradle_version")
    data class GradleVersion(
        @SerialName("version_string") override val versionString: String,
        @SerialName("last_changed") override val lastChanged: Long,
        override val stable: Boolean,
        @SerialName("repository_ids") override val repositoryIds: List<String>,
        val variants: List<Variant>,
        override val vulnerability: Vulnerability,
        val parentComponent: String? = null
    ) : Version

    @Serializable
    data class GradleDependency(
        val group: String,
        val module: String,
        val version: String
    )

    @Serializable(with = GradleVariantSerializer::class)
    sealed interface Variant {

        val name: String
        val attributes: Map<String, String>

        @Serializable
        data class WithFiles(
            override val name: String,
            override val attributes: Map<String, String>,
            val dependencies: List<GradleDependency>,
            val files: List<File>
        ) : Variant

        @Serializable
        data class WithAvailableAt(
            override val name: String,
            override val attributes: Map<String, String>,
            @SerialName("available-at") val availableAt: AvailableAt,
        ) : Variant {

            @Serializable
            data class AvailableAt(
                val url: String,
                val group: String,
                val module: String,
                val version: String
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
            val md5: String
        )
    }
}
