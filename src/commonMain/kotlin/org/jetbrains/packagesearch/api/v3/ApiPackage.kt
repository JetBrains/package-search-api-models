package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.packageversionutils.normalization.NormalizedVersion
import java.security.MessageDigest

/**
 * The base interface for all packages stored in Package Search.
 * When serialized in JSON they will have the field `type`.
 * Available packages types are:
 * - [ApiGradlePackage]s - `type`: "gradle"
 * - [ApiBaseMavenPackage]s - `type`: "maven"
 *
 */
@Serializable
sealed interface ApiPackage {

    val id: String
    val idHash: String
    val name: String?
    val description: String?
    val authors: List<Author>
    val scmUrl: String?
    val licenses: Licenses?
    val rankingMetric: Double?
    val versions: List<ApiPackageVersion>

    companion object {
        fun hashPackageId(id: String) =
            MessageDigest.getInstance("SHA-256")
                .digest(id.toByteArray())
                .joinToString("") { "%02x".format(it) }

    }
}

@Serializable
sealed interface ApiPackageVersion {
    val normalized: NormalizedVersion
    val repositoryIds: List<String>
    val vulnerability: Vulnerability
}

@Serializable
sealed interface ApiMavenPackage : ApiPackage {
    override val versions: List<ApiMavenVersion>
    val groupId: String
    val artifactId: String
}

@Serializable
sealed interface ApiMavenVersion : ApiPackageVersion {
    val dependencies: List<Dependency>
    val artifacts: List<ApiArtifact>
}

@Serializable
@SerialName("maven")
data class ApiBaseMavenPackage(
    override val id: String,
    override val idHash: String,
    override val name: String?,
    override val description: String?,
    override val authors: List<Author>,
    override val scmUrl: String?,
    override val licenses: Licenses?,
    override val rankingMetric: Double?,
    override val versions: List<BaseMavenVersion>,
    override val groupId: String,
    override val artifactId: String
) : ApiMavenPackage {

    @Serializable
    @SerialName("maven_version")
    data class BaseMavenVersion(
        override val normalized: NormalizedVersion,
        override val repositoryIds: List<String>,
        override val vulnerability: Vulnerability,
        override val dependencies: List<Dependency>,
        override val artifacts: List<ApiArtifact>
    ) : ApiMavenVersion

}

@Serializable
data class ApiArtifact(
    val name: String,
    val md5: String,
    val sha1: String,
    val sha256: String,
    val sha512: String
)

@Serializable
@SerialName("gradle")
data class ApiGradlePackage(
    override val id: String,
    override val idHash: String,
    override val name: String?,
    override val description: String?,
    override val authors: List<Author>,
    override val scmUrl: String?,
    override val licenses: Licenses?,
    override val rankingMetric: Double?,
    override val versions: List<GradleVersion>,
    override val groupId: String,
    override val artifactId: String
) : ApiMavenPackage {

    val module: String
        get() = artifactId

    @Serializable
    @SerialName("gradle_version")
    data class GradleVersion(
        override val normalized: NormalizedVersion,
        override val repositoryIds: List<String>,
        val variants: List<ApiVariant>,
        override val vulnerability: Vulnerability,
        val parentComponent: String? = null,
        override val dependencies: List<Dependency>,
        override val artifacts: List<ApiArtifact>
    ) : ApiMavenVersion

    @Serializable
    data class ApiGradleDependency(
        val group: String,
        val module: String,
        val version: String
    )

    @Serializable(with = GradleVariantSerializer::class)
    sealed interface ApiVariant {

        val name: String
        val attributes: Map<String, String>

        @Serializable
        data class WithFiles(
            override val name: String,
            override val attributes: Map<String, String>,
            val dependencies: List<ApiGradleDependency>,
            val files: List<File>
        ) : ApiVariant

        @Serializable
        data class WithAvailableAt(
            override val name: String,
            override val attributes: Map<String, String>,
            @SerialName("available-at") val availableAt: AvailableAt,
        ) : ApiVariant {

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
