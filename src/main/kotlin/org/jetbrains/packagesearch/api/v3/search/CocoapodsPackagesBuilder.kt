package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
class CocoapodsPackagesBuilder internal constructor() {
    private val platformMinTypeMap: MutableMap<CocoapodsPackages.Platform, String> = mutableMapOf()

    fun platform(platform: CocoapodsPackages.Platform, minType: String) {
        platformMinTypeMap[platform] = minType
    }

    fun build() = CocoapodsPackages(platformMinTypeMap)
}

fun buildCocoapodsPackages(block: CocoapodsPackagesBuilder.() -> Unit) =
    CocoapodsPackagesBuilder().apply(block).build()