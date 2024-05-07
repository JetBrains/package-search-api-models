package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MavenHashLookupResponse(
    public val packageInfo: ApiMavenPackage,
    public val version: String,
    public val artifact: ApiArtifact,
)

@Serializable
public sealed interface MavenHashLookupRequest {
    public val hash: String
}

@Serializable
@SerialName("md5")
public data class MavenHashMd5LookupRequest(
    public val md5: String,
) : MavenHashLookupRequest {
    public override val hash: String
        get() = md5
}

@Serializable
@SerialName("sha1")
public data class MavenHashSha1LookupRequest(
    val sha1: String,
) : MavenHashLookupRequest {
    public override val hash: String
        get() = sha1
}

@Serializable
@SerialName("sha256")
public data class MavenHashSha256LookupRequest(
    public val sha256: String,
) : MavenHashLookupRequest {
    public override val hash: String
        get() = sha256
}

@Serializable
@SerialName("sha512")
public data class MavenHashSha512LookupRequest(
    val sha512: String,
) : MavenHashLookupRequest {
    public override val hash: String
        get() = sha512
}
