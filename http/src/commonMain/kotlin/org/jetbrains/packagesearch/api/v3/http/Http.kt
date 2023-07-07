package org.jetbrains.packagesearch.api.v3.http

import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiPackage

@Serializable
public data class GetPackageInfoRequest(
    public val ids: Set<String>,
)

@Serializable
public data class GetPackageInfoResponse(
    public val packages: List<ApiPackage>
)

@Serializable
public data class GetScmByUrlRequest(
    public val urls: List<String>,
)
