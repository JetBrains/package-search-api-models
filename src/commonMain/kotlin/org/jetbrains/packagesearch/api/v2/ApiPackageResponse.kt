package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("ConstructorParameterNaming") // Bug in detekt
@Serializable
data class ApiPackageResponse<T : ApiPackage<V>, V : ApiVersion>(
    @SerialName("package") val `package`: T?,
    @SerialName("repositories") val repositories: List<ApiRepository>,
)
