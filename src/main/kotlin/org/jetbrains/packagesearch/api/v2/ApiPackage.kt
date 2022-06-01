package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName

interface ApiPackage<T : ApiVersion> {

    @SerialName("group_id")
    val groupId: String

    @SerialName("artifact_id")
    val artifactId: String

    @SerialName("versions")
    val versions: List<T>
}
