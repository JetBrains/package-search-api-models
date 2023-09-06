package org.jetbrains.packagesearch.api.v3.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiMavenPackage
import org.jetbrains.packagesearch.api.v3.ApiMavenPackage.GradleVersion.ApiVariant

@Serializable
public sealed interface PackagesType {

    @Serializable
    @SerialName("maven")
    public object Maven : PackagesType

    @Serializable
    @SerialName("npm")
    public object Npm : PackagesType

    @Serializable
    @SerialName("gradle")
    public data class Gradle(
        public val variants: List<Variant>,
        public val mustBeRootPublication: Boolean = true,
    ) : PackagesType {

        @Serializable
        public data class Variant(
            public val attributes: Map<String, ApiVariant.Attribute>,
            public val mustBeWithFiles: Boolean,
        )

    }

    @Serializable
    @SerialName("cocoapods")
    public data class Cocoapods(
        public val platformMinType: Map<Platform, String>,
    ) : PackagesType {
        public enum class Platform(public val platformName: String) {
            IOS("ios"), MACOS("osx"), TVOS("tvos"), WATCHOS("watchos")
        }
    }
}