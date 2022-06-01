package com.jetbrains.packagesearch.api.v1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiMinimalPackage(
    @SerialName("groupId") override val groupId: String,
    @SerialName("artifactId") override val artifactId: String,
    @SerialName("versions") override val versions: List<String>
) : ApiPackage<String>
