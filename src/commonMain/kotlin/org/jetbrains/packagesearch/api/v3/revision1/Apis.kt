package org.jetbrains.packagesearch.api.v3.revision1

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
public data class Vulnerability(
    public val isVulnerable: Boolean,
    public val issues: List<String>? = null,
) {
    public companion object {
        public val NOT_VULNERABLE: Vulnerability = Vulnerability(false)
    }
}
