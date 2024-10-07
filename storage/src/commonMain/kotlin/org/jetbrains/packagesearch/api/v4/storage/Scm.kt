package org.jetbrains.packagesearch.api.v4.storage

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.jetbrains.packagesearch.api.v4.core.ScmId

@Serializable
public sealed interface Scm {
    public val id: ScmId

    @Serializable
    public sealed interface Resolved : Scm {
        public val lastUpdated: Instant
        public val url: String
    }

    @Serializable
    @SerialName("unresolved")
    public data class Unresolved(
        override val id: ScmId,
        val foundAt: Instant = Clock.System.now(),
    ) : Scm

    @Serializable
    @SerialName("errored")
    public data class Errored(
        override val id: ScmId,
        val error: JsonElement,
        val erroredAt: Instant = Clock.System.now(),
    ) : Scm

}

