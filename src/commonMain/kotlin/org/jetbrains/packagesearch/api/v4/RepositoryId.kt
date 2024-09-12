package org.jetbrains.packagesearch.api.v4

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface RepositoryId {

    public val name: String

    @Serializable
    public sealed class Maven : RepositoryId {
        public abstract val url: String
        public open val downloadUrl: String
            get() = url

        override fun toString(): String = name

        @Serializable
        @SerialName("maven_central")
        public data object Central : Maven() {
            override val name: String
                get() = "maven_central"
            override val url: String
                get() = "https://repo.maven.apache.org/maven2/"

            override val downloadUrl: String
                get() = "https://maven-central-eu.storage-download.googleapis.com/maven2/"
        }

        @Serializable
        @SerialName("google_maven")
        public data object GoogleMaven : Maven() {
            override val name: String
                get() = "google_maven"
            override val url: String
                get() = "https://maven.google.com/"
        }

        @Serializable
        @SerialName("clojars")
        public data object Clojars : Maven() {
            override val name: String
                get() = "clojars"
            override val url: String
                get() = "https://clojars.org/repo/"
        }

        @Serializable
        @SerialName("space_maven")
        public data class Space(
            val projectId: String,
            val spaceRepositoryId: String,
            override val name: String,
            override val url: String
        ) : Maven()

    }
}