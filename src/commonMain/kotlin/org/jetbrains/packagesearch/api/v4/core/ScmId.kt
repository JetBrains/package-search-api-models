package org.jetbrains.packagesearch.api.v4.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface ScmId {

    public companion object {
        public fun fromUrl(url: String): ScmId? = when {
            "github" in url -> {
                url.substringAfter("github.com/")
                    .removeSuffix(".git")
                    .removeSuffix("/")
                    .split("/")
                    .filterNot { it.isEmpty() }
                    .take(2)
                    .takeIf { it.size == 2 }
                    ?.let { (owner, name) -> GitHub(owner, name) }
            }

            else -> null
        }
    }

    @Serializable
    @SerialName("github")
    public data class GitHub(
        val owner: String,
        val name: String
    ) : ScmId
}