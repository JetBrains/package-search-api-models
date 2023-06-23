package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
class PackagesTypeBuilder {
    private val packagesType: MutableList<PackagesType> = mutableListOf()

    fun mavenPackages() {
        packagesType.add(MavenPackages)
    }

    fun npmPackages() {
        packagesType.add(NpmPackages)
    }

    fun gradlePackages(gradlePackages: GradlePackages) {
        packagesType.add(gradlePackages)
    }

    fun gradlePackages(variants: List<GradlePackages.Variant>, isRootPublication: Boolean = true) {
        gradlePackages(
            buildGradlePackages {
                variants(variants)
                this.mustBeRootPublication = isRootPublication
            },
        )
    }

    fun gradlePackages(block: GradlePackagesBuilder.() -> Unit) {
        gradlePackages(buildGradlePackages(block))
    }

    fun cocoapodsPackages(cocoapodsPackages: CocoapodsPackages) {
        packagesType.add(cocoapodsPackages)
    }

    fun cocoapodsPackages(platformMinType: Map<CocoapodsPackages.Platform, String>) {
        cocoapodsPackages(
            buildCocoapodsPackages {
                platformMinType.forEach { platform(it.key, it.value) }
            },
        )
    }

    fun cocoapodsPackages(block: CocoapodsPackagesBuilder.() -> Unit) {
        cocoapodsPackages(buildCocoapodsPackages(block))
    }

    fun build() = packagesType.distinct()
}

fun buildPackageTypes(block: PackagesTypeBuilder.() -> Unit) =
    PackagesTypeBuilder().apply(block).build()
