package org.jetbrains.packagesearch.api.v3.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.api.v3.ApiMavenPackage.ApiVariant.Attribute

@Serializable
data class SearchParameters(
    val packagesType: List<PackagesType>,
    val searchQuery: String,
)

@Serializable
sealed interface PackagesType

@Serializable
@SerialName("maven")
object MavenPackages : PackagesType

@Serializable
@SerialName("npm")
object NpmPackages : PackagesType

@Serializable
@SerialName("gradle")
data class GradlePackages(
    val variants: List<Variant>,
    val mustBeRootPublication: Boolean = true,
) : PackagesType {

    @Serializable
    data class Variant(
        val attributes: Map<String, Attribute>,
        val withFiles: Boolean,
    )

}

@Serializable
@SerialName("cocoapods")
data class CocoapodsPackages(
    val platformMinType: Map<Platform, String>,
) : PackagesType {
    enum class Platform(val platformName: String) {
        IOS("ios"), MACOS("osx"), TVOS("tvos"), WATCHOS("watchos")
    }
}
