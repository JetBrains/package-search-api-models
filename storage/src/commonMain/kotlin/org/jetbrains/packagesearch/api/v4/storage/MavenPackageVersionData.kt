package org.jetbrains.packagesearch.api.v4.storage

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v4.core.PackageId
import org.jetbrains.packagesearch.api.v4.core.PackageRepository
import org.jetbrains.packagesearch.api.v4.core.ScmId
import org.jetbrains.packagesearch.gradle.GradleMetadata
import org.jetbrains.packagesearch.kotlin.KotlinMetadata
import org.jetbrains.packagesearch.maven.MavenArtifactInfo
import org.jetbrains.packagesearch.maven.MavenProjectObjectModel

@Serializable
@SerialName("maven")
public data class MavenPackageVersionData(
    override val packageRepository: PackageRepository.Maven,
    override val packageId: PackageId.Maven,
    override val versionString: String,
    override val scmId: ScmId?,
    val pom: MavenProjectObjectModel,
    val gradleMetadata: GradleMetadata? = null,
    val kotlinMetadata: KotlinMetadata? = null,
    val javaInfo: JavaInfo? = null,
    val scalaCompilerVersion: String? = null,
    val artifacts: Set<MavenArtifactInfo> = emptySet(),
    override val publishedAt: Instant,
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