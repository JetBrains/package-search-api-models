package com.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName

interface ApiVersion {

    @SerialName("version")
    val version: String

    @SerialName("repository_ids")
    val repositoryIds: List<String>
}
