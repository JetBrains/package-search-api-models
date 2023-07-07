package org.jetbrains.packagesearch.api.v3.search

import org.jetbrains.packagesearch.api.v3.ApiMavenPackage.ApiVariant.Attribute
import org.jetbrains.packagesearch.api.v3.search.GradlePackages.Variant

@SearchParametersBuilderDsl
public class GradlePackagesBuilder internal constructor() {
    private val variants: MutableList<Variant> = mutableListOf()
    public var mustBeRootPublication: Boolean = true

    public fun variants(variants: List<Variant>) {
        this.variants.addAll(variants)
    }

    @SearchParametersBuilderDsl
    public class VariantBuilder internal constructor() {
        private val attributes: MutableMap<String, Attribute> = mutableMapOf()
        public var mustHaveFilesAttribute: Boolean = false

        public fun attribute(key: String, value: String) {
            attributes[key] = Attribute.create(key, value)
        }

        public fun attribute(key: String, value: Attribute) {
            attributes[key] = value
        }

        public fun build(): Variant = Variant(attributes.toMap(), mustHaveFilesAttribute)
    }

    public fun buildVariant(block: VariantBuilder.() -> Unit): Variant = VariantBuilder().apply(block).build()

    public fun variant(block: VariantBuilder.() -> Unit) {
        variants.add(buildVariant(block))
    }

    internal fun build(): GradlePackages = GradlePackages(variants.toList(), mustBeRootPublication)
}

public fun buildGradlePackages(block: GradlePackagesBuilder.() -> Unit): GradlePackages =
    GradlePackagesBuilder().apply(block).build()

public fun GradlePackagesBuilder.VariantBuilder.kotlinPlatformType(platformType: String) {
    attribute("org.jetbrains.kotlin.platform.type", platformType)
}

public fun GradlePackagesBuilder.VariantBuilder.native(platform: String) {
    kotlinPlatformType("native")
    attribute("org.jetbrains.kotlin.native.target", platform)
}

public fun GradlePackagesBuilder.VariantBuilder.jvm(): Unit {
    kotlinPlatformType("jvm")
}
public fun GradlePackagesBuilder.VariantBuilder.js(legacyCompiler: Boolean) {
    kotlinPlatformType("js")
    val compilerAttribute = if (legacyCompiler) "legacy" else "ir"
    attribute("org.jetbrains.kotlin.js.compiler", compilerAttribute)
}

public fun GradlePackagesBuilder.VariantBuilder.jsLegacy() {
    js(legacyCompiler = true)
}
public fun GradlePackagesBuilder.VariantBuilder.jsIr() {
    js(legacyCompiler = false)
}

public fun GradlePackagesBuilder.VariantBuilder.category(category: String) {
    attribute("org.gradle.category", category)
}

public fun GradlePackagesBuilder.VariantBuilder.libraryElements(elements: String) {
    attribute("org.gradle.libraryelements", elements)
}

public fun GradlePackagesBuilder.VariantBuilder.usage(usage: String) {
    attribute("org.gradle.usage", usage)
}
public fun GradlePackagesBuilder.VariantBuilder.libraryCategory() {
    category("library")
}

public fun GradlePackagesBuilder.VariantBuilder.javaApi() {
    usage("java-api")
}
public fun GradlePackagesBuilder.VariantBuilder.javaRuntime() {
    usage("java-runtime")
}
public fun GradlePackagesBuilder.VariantBuilder.kotlinMetadata() {
    libraryCategory()
    usage("kotlin-metadata")
    kotlinPlatformType("common")
}
