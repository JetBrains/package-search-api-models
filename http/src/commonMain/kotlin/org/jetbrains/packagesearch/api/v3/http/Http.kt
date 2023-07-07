package org.jetbrains.packagesearch.api.v3.http

import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiPackage

@Serializable
data class GetPackageInfoRequest(
    val ids: Set<String>,
)

@Serializable
data class GetPackageInfoResponse(
    val packages: List<ApiPackage>
)

@Serializable
data class GetScmByUrlRequest(
    val urls: List<String>,
)
