package com.jetbrains.packagesearch.api.statistics.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiScmStats(
    @SerialName("git") val git: Int,
    @SerialName("svn") val svn: Int,
    @SerialName("mercurial") val mercurial: Int,
    @SerialName("others") val others: Int,
)
