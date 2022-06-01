package com.jetbrains.packagesearch.api.statistics.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiCodeHostingPlatform(
    @SerialName("github") val github: Int,
    @SerialName("gitlab") val gitlab: Int,
    @SerialName("bitbucket") val bitbucket: Int,
    @SerialName("space") val space: Int,
    @SerialName("others") val others: Int,
)
