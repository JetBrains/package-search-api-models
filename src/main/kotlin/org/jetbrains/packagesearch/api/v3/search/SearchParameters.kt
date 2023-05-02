package org.jetbrains.packagesearch.api.v3.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchParameters(
    val onlyStable: Boolean = true,
    val packagesType: List<PackagesType>,
    val searchQuery: String
)

@Serializable
sealed interface PackagesType

@Serializable
@SerialName("maven-packages")
object MavenPackages : PackagesType

@Serializable
@SerialName("npm-packages")
object NpmPackages : PackagesType

@Serializable
@SerialName("gradle-packages")
data class GradlePackages(
    val variants: List<Map<String, String>>,
    val isRootPublication: Boolean = true
) : PackagesType

@Serializable
@SerialName("cocoapods-packages")
data class CocoapodsPackages(
    val platformMinType: Map<Platform, String>
) : PackagesType {
    enum class Platform(val platformName: String) {
        IOS("ios"), MACOS("osx"), TVOS("tvos"), WATCHOS("watchos")
    }
}
