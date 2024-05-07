package org.jetbrains.packagesearch.api.v3

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface ApiRepository {

    public val id: String
    public val lastChecked: Instant?
    public val name: String
    public val url: String
}

@Serializable
@SerialName("maven")
public data class ApiMavenRepository(
    public override val id: String,
    public override val lastChecked: Instant?,
    public override val url: String,
    public val alternateUrls: List<String>,
    public val friendlyName: String,
    public val userFacingUrl: String? = null,
    public val packageCount: Int? = null,
    public val artifactCount: Int? = null,
    public val namedLinks: String? = null,
) : ApiRepository {
    public override val name: String
        get() = friendlyName
}

//@Serializable
//@SerialName("cocoapods")
//public object ApiCocoapodsRepository : ApiRepository {
//
//    public override val id: String = "cocoapods"
//    public override val lastChecked: Instant? = null
//}
//
//@Serializable
//@SerialName("npm")
//public object ApiNpmRepository : ApiRepository {
//
//    public override val id: String = "npm"
//    public override val lastChecked: Instant? = null
//}
