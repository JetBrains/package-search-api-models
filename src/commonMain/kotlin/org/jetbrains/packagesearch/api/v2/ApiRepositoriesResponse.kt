package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiRepositoriesResponse(
    @SerialName("repositories") val repositories: List<ApiRepository>,
)
