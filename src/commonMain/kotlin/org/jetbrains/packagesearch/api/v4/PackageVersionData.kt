package org.jetbrains.packagesearch.api.v4

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.jetbrains.packagesearch.gradle.GradleMetadata
import org.jetbrains.packagesearch.kotlin.KotlinMetadata
import org.jetbrains.packagesearch.maven.MavenArtifactInfo
import org.jetbrains.packagesearch.maven.MavenProjectObjectModel

@Serializable
public sealed interface PackageVersionData {
    public val repositoryId: RepositoryId
    public val packageId: PackageId
    public val versionString: String

    @Serializable
    public sealed interface Resolved : PackageVersionData {
        public val publishedAt: Instant?
        public val scmId: ScmId?
    }

    @Serializable
    @SerialName("errored")
    public data class Errored(
        override val repositoryId: RepositoryId,
        override val packageId: PackageId,
        override val versionString: String,
        val erroredAt: Instant?,
        val error: JsonElement? = null,
    ) : PackageVersionData

}

@Serializable
@SerialName("maven")
public data class MavenPackageVersionData(
    override val repositoryId: RepositoryId.Maven,
    override val packageId: PackageId.Maven,
    override val versionString: String,
    override val scmId: ScmId?,
    val pom: MavenProjectObjectModel,
    val gradleMetadata: GradleMetadata? = null,
    val kotlinMetadata: KotlinMetadata? = null,
    val javaInfo: JavaInfo? = null,
    val artifacts: Set<MavenArtifactInfo> = emptySet(),
    override val publishedAt: Instant? = null,
) : PackageVersionData.Resolved {

    @Serializable
    public data class JavaInfo(val bytecodeVersion: BytecodeVersion, val javaVersion: Int)

    public companion object {
        public fun JavaInfo(bytecodeMajor: Int, bytecodeMinor: Int): JavaInfo =
            JavaInfo(BytecodeVersion(bytecodeMajor, bytecodeMinor), bytecodeMajor - 44)
    }

    @Serializable
    public data class BytecodeVersion(val major: Int, val minor: Int)

}


/**
 * Data class representing a package.
 * @property packageId The id of the package
 * @property allVersions The versions of the package per repository
 */
@Serializable
public data class PackageData(
    val packageId: PackageId,
    val latestVersion: VersionInfo,
    val latestStableVersion: VersionInfo? = null,
    val allVersions: Map<String, List<String>>,
    val lastUpdated: Instant = Clock.System.now()
) {
    @Serializable
    public data class VersionInfo(
        val version: String,
        val publishedAt: Instant? = null,
        val repositoryId: RepositoryId
    )
}
