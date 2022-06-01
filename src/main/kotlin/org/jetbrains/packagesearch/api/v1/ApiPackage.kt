package org.jetbrains.packagesearch.api.v1

import kotlinx.serialization.SerialName

interface ApiPackage<T> {

    @SerialName("groupId")
    val groupId: String

    @SerialName("artifactId")
    val artifactId: String

    @SerialName("versions")
    val versions: List<T>
}
