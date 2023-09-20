package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.Serializable

@Serializable
public data class Licenses<T: LicenseFile>(
    public val mainLicense: T,
    public val otherLicenses: List<T> = emptyList(),
)

@Serializable
public sealed interface LicenseFile {
    public val name: String?
    public val url: String
}

@Serializable
public data class PomLicenseFile(
    public override val name: String? = null,
    public override val url: String,
) : LicenseFile

@Serializable
public data class ScmLicenseFile(
    public override val name: String? = null,
    public override val url: String,
    public val htmlUrl: String? = null,
    public val spdxId: String? = null,
    public val key: String? = null,
) : LicenseFile