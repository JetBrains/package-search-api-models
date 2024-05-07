package org.jetbrains.packagesearch.api.v3.revision1.search

@SearchParametersBuilderDsl
public class CocoapodsPackagesBuilder internal constructor() {
    private val platformMinTypeMap: MutableMap<PackagesType.Cocoapods.Platform, String> = mutableMapOf()

    public fun platform(
        platform: PackagesType.Cocoapods.Platform,
        minType: String,
    ) {
        platformMinTypeMap[platform] = minType
    }

    public fun build(): PackagesType.Cocoapods = PackagesType.Cocoapods(platformMinTypeMap)
}

public fun buildCocoapodsPackages(block: CocoapodsPackagesBuilder.() -> Unit): PackagesType.Cocoapods =
    CocoapodsPackagesBuilder().apply(block).build()
