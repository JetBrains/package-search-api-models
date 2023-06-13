package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.Serializable

@Serializable
data class Dependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val scope: String? = null
)

@Serializable
data class Author(
    val name: String? = null,
    val email: String? = null,
    val org: String? = null,
    val orgUrl: String? = null
)

@Serializable
data class Licenses(
    val mainLicense: LicenseFile,
    val otherLicenses: List<LicenseFile>? = null
)

@Serializable
data class LicenseFile(
    val name: String? = null,
    val url: String,
    val htmlUrl: String? = null,
    val spdxId: String? = null,
    val key: String? = null
)

@Serializable
data class Vulnerability(
    val isVulnerable: Boolean,
    val issues: List<String> = emptyList()
) {
    companion object {
        val NOT_VULNERABLE = Vulnerability(false)
    }
}

