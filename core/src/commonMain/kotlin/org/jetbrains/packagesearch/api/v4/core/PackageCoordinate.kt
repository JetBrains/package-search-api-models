package org.jetbrains.packagesearch.api.v4.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.packageversionutils.normalization.NormalizedVersion

@Serializable
public sealed interface PackageCoordinate {
    public val packageRepository: PackageRepository
    public val packageId: PackageId
    public val version: String

    @Serializable
    @SerialName("maven")
    public data class Maven(
        override val packageRepository: PackageRepository.Maven,
        override val packageId: PackageId.Maven,
        override val version: String
    ) : PackageCoordinate

}

@Serializable
public sealed interface CompletedDownloadPackageCoordinate {
    public val packageRepository: PackageRepository
    public val packageId: PackageId
    public val version: NormalizedVersion

    @Serializable
    @SerialName("maven")
    public data class Maven(
        override val packageRepository: PackageRepository.Maven,
        override val packageId: PackageId.Maven,
        override val version: NormalizedVersion
    ) : CompletedDownloadPackageCoordinate

}