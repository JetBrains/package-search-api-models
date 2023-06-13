package org.jetbrains.packagesearch.api.v3.http

import kotlinx.serialization.Serializable

@Serializable
data class GetPackageInfoRequest(
    val ids: Set<String>
)

@Serializable
data class GetScmByUrlRequest(
    val urls: List<String>
)
