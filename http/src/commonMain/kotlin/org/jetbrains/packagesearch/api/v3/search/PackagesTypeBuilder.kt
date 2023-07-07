package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
public class PackagesTypeBuilder {
    private val packagesType: MutableList<PackagesType> = mutableListOf()

    public fun mavenPackages() {
        packagesType.add(MavenPackages)
    }

    public fun npmPackages() {
        packagesType.add(NpmPackages)
    }

    public fun gradlePackages(gradlePackages: GradlePackages) {
        packagesType.add(gradlePackages)
    }

    public fun gradlePackages(variants: List<GradlePackages.Variant>, isRootPublication: Boolean = true) {
        gradlePackages(
            buildGradlePackages {
                variants(variants)
                this.mustBeRootPublication = isRootPublication
            },
        )
    }

    public fun gradlePackages(block: GradlePackagesBuilder.() -> Unit) {
        gradlePackages(buildGradlePackages(block))
    }

    public fun cocoapodsPackages(cocoapodsPackages: CocoapodsPackages) {
        packagesType.add(cocoapodsPackages)
    }

    public fun cocoapodsPackages(platformMinType: Map<CocoapodsPackages.Platform, String>) {
        cocoapodsPackages(
            buildCocoapodsPackages {
                platformMinType.forEach { platform(it.key, it.value) }
            },
        )
    }

    public fun cocoapodsPackages(block: CocoapodsPackagesBuilder.() -> Unit) {
        cocoapodsPackages(buildCocoapodsPackages(block))
    }

    public fun build(): List<PackagesType> = packagesType.distinct()
}

public fun buildPackageTypes(block: PackagesTypeBuilder.() -> Unit): List<PackagesType> =
    PackagesTypeBuilder().apply(block).build()
