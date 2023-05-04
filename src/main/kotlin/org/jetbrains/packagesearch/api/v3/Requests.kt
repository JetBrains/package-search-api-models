package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MavenHashLookupResponse(
    val packageInfo: ApiMavenPackage<ApiMavenVersion>,
    val version: String,
    val artifact: ApiArtifact
)

@Serializable
sealed interface MavenHashLookupRequest {
    val hash: String
}

@Serializable
@SerialName("md5")
data class MavenHashMd1LookupRequest(
    val md1: String
) : MavenHashLookupRequest {
    override val hash: String
        get() = md1
}

@Serializable
@SerialName("sha1")
data class MavenHashSha1LookupRequest(
    val sha1: String
) : MavenHashLookupRequest {
    override val hash: String
        get() = sha1
}

@Serializable
@SerialName("sha256")
data class MavenHashSha256LookupRequest(
    val sha256: String
) : MavenHashLookupRequest {
    override val hash: String
        get() = sha256
}

@Serializable
@SerialName("sha512")
data class MavenHashSha512LookupRequest(
    val sha512: String
) : MavenHashLookupRequest {
    override val hash: String
        get() = sha512
}
