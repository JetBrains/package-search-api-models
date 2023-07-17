package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
public class CocoapodsPackagesBuilder internal constructor() {
    private val platformMinTypeMap: MutableMap<CocoapodsPackages.Platform, String> = mutableMapOf()

    public fun platform(platform: CocoapodsPackages.Platform, minType: String) {
        platformMinTypeMap[platform] = minType
    }

    public fun build(): CocoapodsPackages = CocoapodsPackages(platformMinTypeMap)
}

public fun buildCocoapodsPackages(block: CocoapodsPackagesBuilder.() -> Unit): CocoapodsPackages =
    CocoapodsPackagesBuilder().apply(block).build()
