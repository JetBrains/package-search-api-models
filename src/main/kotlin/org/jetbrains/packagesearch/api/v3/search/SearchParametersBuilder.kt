package org.jetbrains.packagesearch.api.v3.search

@DslMarker
annotation class SearchParametersBuilderDsl

@SearchParametersBuilderDsl
class SearchParametersBuilder {
    var onlyStable: Boolean = true
    private val packagesType: MutableList<PackagesType> = mutableListOf()

    fun packagesType(packagesType: PackagesType) {
        this.packagesType.add(packagesType)
    }

    fun mavenPackages() {
        packagesType.add(MavenPackages)
    }

    fun npmPackages() {
        packagesType.add(NpmPackages)
    }

    fun gradlePackages(variants: List<Map<String, String>>, isRootPublication: Boolean = true) {
        packagesType.add(GradlePackages(variants, isRootPublication))
    }


    fun gradlePackages(block: GradlePackagesBuilder.() -> Unit) {
        packagesType.add(GradlePackagesBuilder().apply(block).build())
    }

    fun cocoapodsPackages(platformMinTypeMap: Map<CocoapodsPackages.Platform, String>) {
        packagesType.add(CocoapodsPackages(platformMinTypeMap))
    }


    fun cocoapodsPackages(block: CocoapodsPackagesBuilder.() -> Unit) {
        packagesType.add(CocoapodsPackagesBuilder().apply(block).build())
    }

    fun build() = SearchParameters(onlyStable, packagesType)
}

@SearchParametersBuilderDsl
class CocoapodsPackagesBuilder {
    private val platformMinTypeMap: MutableMap<CocoapodsPackages.Platform, String> = mutableMapOf()

    fun platform(platform: CocoapodsPackages.Platform, minType: String) {
        platformMinTypeMap[platform] = minType
    }

    fun build() = CocoapodsPackages(platformMinTypeMap)
}

fun buildSearchParameters(block: SearchParametersBuilder.() -> Unit) =
    SearchParametersBuilder().apply(block).build()

