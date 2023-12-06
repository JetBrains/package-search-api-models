package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
public class PackagesTypeBuilder {
    private val packagesType: MutableList<PackagesType> = mutableListOf()

    public fun mavenPackages() {
        packagesType.add(PackagesType.Maven)
    }

    public fun npmPackages() {
        packagesType.add(PackagesType.Npm)
    }

    public fun gradlePackages(gradlePackages: PackagesType.Gradle) {
        packagesType.add(gradlePackages)
    }

    public fun gradlePackages(variants: List<PackagesType.Gradle.Variant>, isRootPublication: Boolean = true) {
        gradlePackages(
            buildGradlePackages {
                variants(variants)
                this.isRootPublication = isRootPublication
            },
        )
    }

    public fun gradlePackages(block: GradlePackagesBuilder.() -> Unit) {
        gradlePackages(buildGradlePackages(block))
    }

    public fun cocoapodsPackages(cocoapodsPackages: PackagesType.Cocoapods) {
        packagesType.add(cocoapodsPackages)
    }

    public fun cocoapodsPackages(platformMinType: Map<PackagesType.Cocoapods.Platform, String>) {
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

@SearchParametersBuilderDsl
public fun buildPackageTypes(block: PackagesTypeBuilder.() -> Unit): List<PackagesType> =
    PackagesTypeBuilder().apply(block).build()

public fun PackagesTypeBuilder.jvmGradlePackages(packaging: String) {
    mavenPackages()
    gradlePackages {
        isRootPublication = true
        variant {
            javaApi()
            libraryElements(packaging)
        }
        variant {
            javaRuntime()
            libraryElements(packaging)
        }
    }
}

public fun PackagesTypeBuilder.androidPackages() {
    jvmGradlePackages("aar")
    jvmGradlePackages("jar")
    gradlePackages {
        isRootPublication = true
        variant {
            kotlinPlatformType("android")
        }
    }
}

public fun PackagesTypeBuilder.jvmMavenPackages() {
    mavenPackages()
    gradlePackages {
        isRootPublication = false
        variant {
            haveFiles = true
            javaApi()
        }
        variant {
            haveFiles = true
            javaRuntime()
        }
    }
}
