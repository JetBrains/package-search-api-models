package org.jetbrains.packagesearch.api.v4.storage

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v4.core.PackageId
import org.jetbrains.packagesearch.api.v4.core.PackageRepository

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
    val allVersions: Map<String, Set<String>>,
    val lastUpdated: Instant = Clock.System.now()
) {
    @Serializable
    public data class VersionInfo(
        val version: String,
        val publishedAt: Instant? = null,
        val packageRepository: PackageRepository
    )
}