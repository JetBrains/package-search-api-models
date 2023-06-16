package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiPackagesResponse<T : ApiPackage<V>, V : ApiVersion>(
    @SerialName("packages") val packages: List<T>,
    @SerialName("repositories") val repositories: List<ApiRepository>,
)
