package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
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
    @SerialName("org_url") val orgUrl: String? = null
)

@Serializable
data class Licenses(
    @SerialName("main_license") val mainLicense: LinkedFile,
    @SerialName("other_licenses") val otherLicenses: List<LinkedFile>? = null
)

@Serializable
data class LinkedFile(
    val name: String? = null,
    val url: String,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("spdx_id") val spdxId: String? = null,
    val key: String? = null
)

@Serializable
data class Vulnerability(
    @SerialName("is_vulnerable") val isVulnerable: Boolean,
    val issues: List<String> = emptyList()
)

