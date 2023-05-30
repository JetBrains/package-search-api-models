package org.jetbrains.packagesearch.api.v3.http

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class GetPackageInfoRequest(
    val ids: Set<String>
)

@JvmInline
value class GetScmByUrlRequest(
    val url: String
)