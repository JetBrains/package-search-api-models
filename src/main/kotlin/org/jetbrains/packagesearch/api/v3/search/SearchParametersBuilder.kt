package org.jetbrains.packagesearch.api.v3.search

import kotlin.reflect.KClass

@DslMarker
annotation class SearchParametersBuilderDsl

@SearchParametersBuilderDsl
class SearchParametersBuilder internal constructor() {

    var onlyStable: Boolean = true
    var searchQuery: String? = null

    private val packagesType: MutableMap<KClass<out PackagesType>, PackagesType> = mutableMapOf()

    fun mavenPackages() {
        packagesType[MavenPackages::class] = MavenPackages
    }

    fun npmPackages() {
        packagesType[NpmPackages::class] = NpmPackages
    }

    fun gradlePackages(gradlePackages: GradlePackages) {
        val previous = packagesType[GradlePackages::class] as? GradlePackages
        packagesType[GradlePackages::class] =
            previous?.copy(variants = previous.variants + gradlePackages.variants)
                ?: gradlePackages
    }

    fun gradlePackages(variants: List<Map<String, String>>, isRootPublication: Boolean = true) {
        gradlePackages(buildGradlePackages {
            variants(variants)
            this.isRootPublication = isRootPublication
        })
    }


    fun gradlePackages(block: GradlePackagesBuilder.() -> Unit) {
        gradlePackages(buildGradlePackages(block))
    }

    fun cocoapodsPackages(cocoapodsPackages: CocoapodsPackages) {
        val previous = packagesType[CocoapodsPackages::class] as? CocoapodsPackages
        packagesType[CocoapodsPackages::class] =
            previous?.copy(platformMinType = previous.platformMinType + cocoapodsPackages.platformMinType)
                ?: cocoapodsPackages
    }

    fun cocoapodsPackages(platformMinType: Map<CocoapodsPackages.Platform, String>) {
        cocoapodsPackages(buildCocoapodsPackages {
            platformMinType.forEach { platform(it.key, it.value) }
        })
    }


    fun cocoapodsPackages(block: CocoapodsPackagesBuilder.() -> Unit) {
        cocoapodsPackages(buildCocoapodsPackages(block))
    }

    internal fun build(): SearchParameters {
        val query = searchQuery
        val errorText = "Search query is null or blank."
        requireNotNull(query) { errorText }
        require(query.isNotBlank()) { errorText }
        return SearchParameters(
            onlyStable = onlyStable,
            packagesType = packagesType.values.toList(),
            searchQuery = query
        )
    }
}

fun buildSearchParameters(block: SearchParametersBuilder.() -> Unit) =
    SearchParametersBuilder().apply(block).build()

