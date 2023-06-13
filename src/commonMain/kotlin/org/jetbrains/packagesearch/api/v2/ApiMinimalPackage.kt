package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiMinimalPackage(
    @SerialName("group_id") override val groupId: String,
    @SerialName("artifact_id") override val artifactId: String,
    @SerialName("versions") override val versions: List<ApiMinimalVersion>
) : ApiPackage<ApiMinimalPackage.ApiMinimalVersion> {

    @Serializable
    data class ApiMinimalVersion(
        @SerialName("version") override val version: String,
        @SerialName("repository_ids") override val repositoryIds: List<String>
    ) : ApiVersion
}
