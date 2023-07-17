package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.Serializable

@Serializable
public data class Dependency(
    public val groupId: String,
    public val artifactId: String,
    public val version: String,
    public val scope: String? = null,
)

@Serializable
public data class Author(
    public val name: String? = null,
    public val email: String? = null,
    public val org: String? = null,
    public val orgUrl: String? = null,
)

@Serializable
public data class Licenses(
    public val mainLicense: LicenseFile,
    public val otherLicenses: List<LicenseFile> = emptyList(),
)

@Serializable
public data class LicenseFile(
    public val name: String? = null,
    public val url: String,
    public val htmlUrl: String? = null,
    public val spdxId: String? = null,
    public val key: String? = null,
)

@Serializable
public data class Vulnerability(
    public val isVulnerable: Boolean,
    public val issues: List<String> = emptyList(),
) {
    public companion object {
        public val NOT_VULNERABLE: Vulnerability = Vulnerability(false)
    }
}
