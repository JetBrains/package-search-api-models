package org.jetbrains.packagesearch.api.v3.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiMavenPackage.ApiVariant.Attribute

@Serializable
public data class SearchParameters(
    public val packagesType: List<PackagesType>,
    public val searchQuery: String,
)

@Serializable
public sealed interface PackagesType

@Serializable
@SerialName("maven")
public object MavenPackages : PackagesType

@Serializable
@SerialName("npm")
public object NpmPackages : PackagesType

@Serializable
@SerialName("gradle")
public data class GradlePackages(
    public val variants: List<Variant>,
    public val mustBeRootPublication: Boolean = true,
) : PackagesType {

    @Serializable
    public data class Variant(
        public val attributes: Map<String, Attribute>,
        public val withFiles: Boolean,
    )

}

@Serializable
@SerialName("cocoapods")
public data class CocoapodsPackages(
    public val platformMinType: Map<Platform, String>,
) : PackagesType {
    public enum class Platform(public val platformName: String) {
        IOS("ios"), MACOS("osx"), TVOS("tvos"), WATCHOS("watchos")
    }
}
