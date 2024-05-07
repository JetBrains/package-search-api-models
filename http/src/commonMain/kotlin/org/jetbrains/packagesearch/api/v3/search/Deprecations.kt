package org.jetbrains.packagesearch.api.v3.search

import org.jetbrains.packagesearch.api.v3.ApiMavenPackage.GradleVersion.ApiVariant

@Deprecated("Use PackagesType.Gradle")
public object GradlePackagesType {
    @Deprecated("Use PackagesType.Gradle", ReplaceWith("PackagesType.Gradle(variants, mustBeRootPublication)"))
    public operator fun invoke(
        variants: List<PackagesType.Gradle.Variant>,
        mustBeRootPublication: Boolean = true,
    ): PackagesType.Gradle = PackagesType.Gradle(variants, mustBeRootPublication)

    @Deprecated("Use PackagesType.Gradle.Variants", ReplaceWith("PackagesType.Gradle.Variant(attributes, mustBeWithFiles)"))
    @Suppress("function-naming")
    public fun Variant(
        attributes: Map<String, ApiVariant.Attribute>,
        mustBeWithFiles: Boolean,
    ): PackagesType.Gradle.Variant = PackagesType.Gradle.Variant(attributes, mustBeWithFiles)
}

@Deprecated(
    message = "Use PackagesType.Maven",
    replaceWith = ReplaceWith("PackagesType.Maven"),
)
public typealias MavenPackageType = PackagesType.Maven

@Deprecated(
    message = "Use PackagesType.Npm",
    replaceWith = ReplaceWith("PackagesType.Npm"),
)
public typealias NpmPackageType = PackagesType.Npm

@Deprecated(
    message = "Use PackagesType.Cocoapods",
    replaceWith = ReplaceWith("PackagesType.Cocoapods"),
)
public typealias CocoapodsPackageType = PackagesType.Cocoapods
