package org.jetbrains.packagesearch.api.v4.storage

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.jetbrains.packagesearch.api.v4.core.PackageId
import org.jetbrains.packagesearch.api.v4.core.PackageRepository
import org.jetbrains.packagesearch.api.v4.core.ScmId

@Serializable
public sealed interface PackageVersionData {
    public val packageRepository: PackageRepository
    public val packageId: PackageId
    public val versionString: String

    @Serializable
    public sealed interface Resolved : PackageVersionData {
        public val publishedAt: Instant
        public val scmId: ScmId?
    }

    @Serializable
    @SerialName("errored")
    public data class Errored(
        override val packageRepository: PackageRepository,
        override val packageId: PackageId,
        override val versionString: String,
        val erroredAt: Instant?,
        val error: JsonElement? = null,
    ) : PackageVersionData

}