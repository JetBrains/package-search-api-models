package com.jetbrains.packagesearch.api.v2

import kotlinx.serialization.Serializable

@Serializable
data class ApiResultSelection(
    val requestId: String,
    val token: String,
    val resultId: String
)
