package org.jetbrains.packagesearch.api.v4.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface PackageRepository {

    public val name: String

    @Serializable
    @SerialName("maven")
    public data class Maven(
        override val name: String,
        val url: String,
        val downloadUrl: String = url,
        val htmlUiUrl: String? = url,
    ) : PackageRepository {

        override fun toString(): String =
            "$name: $url"

        public companion object {
            public val Central: Maven = Maven(
                name = "maven_central",
                url = "https://repo.maven.apache.org/maven2/",
                downloadUrl = "https://maven-central-eu.storage-download.googleapis.com/maven2/"
            )

            public val GoogleMaven: Maven = Maven(
                name = "google_maven",
                url = "https://maven.google.com/",
            )

            public val Clojars: Maven = Maven(
                name = "clojars",
                url = "https://repo.clojars.org/",
            )
        }
    }

}