package org.jetbrains.packagesearch.api.v3.search

import org.jetbrains.packagesearch.api.v3.ApiGradlePackage
import org.jetbrains.packagesearch.api.v3.ApiGradlePackage.ApiVariant.Attribute
import org.jetbrains.packagesearch.api.v3.search.GradlePackages.Variant

@SearchParametersBuilderDsl
class GradlePackagesBuilder internal constructor() {
    private val variants: MutableList<Variant> = mutableListOf()
    var mustBeRootPublication: Boolean = true

    fun variants(variants: List<Variant>) {
        this.variants.addAll(variants)
    }

    @SearchParametersBuilderDsl
    class VariantBuilder internal constructor() {
        private val attributes: MutableMap<String, Attribute> = mutableMapOf()
        var mustHaveFilesAttribute: Boolean = false

        fun attribute(key: String, value: String) {
            attributes[key] = if (key == "org.gradle.jvm.version")
                Attribute.ComparableInteger(value.toInt())
            else Attribute.ExactMatch(value)
        }

        fun attribute(key: String, value: Attribute) {
            attributes[key] = value
        }

        fun build() = Variant(attributes.toMap(), mustHaveFilesAttribute)
    }

    fun buildVariant(block: VariantBuilder.() -> Unit) = VariantBuilder().apply(block).build()

    fun variant(block: VariantBuilder.() -> Unit) {
        variants.add(buildVariant(block))
    }

    internal fun build() = GradlePackages(variants.toList(), mustBeRootPublication)
}

fun buildGradlePackages(block: GradlePackagesBuilder.() -> Unit) =
    GradlePackagesBuilder().apply(block).build()

fun GradlePackagesBuilder.VariantBuilder.kotlinPlatformType(platformType: String) =
    attribute("org.jetbrains.kotlin.platform.type", platformType)

fun GradlePackagesBuilder.VariantBuilder.native(platform: String) {
    kotlinPlatformType("native")
    attribute("org.jetbrains.kotlin.native.target", platform)
}

fun GradlePackagesBuilder.VariantBuilder.jvm() = kotlinPlatformType("jvm")
fun GradlePackagesBuilder.VariantBuilder.js(legacyCompiler: Boolean) {
    kotlinPlatformType("js")
    val compilerAttribute = if (legacyCompiler) "legacy" else "ir"
    attribute("org.jetbrains.kotlin.js.compiler", compilerAttribute)
}

fun GradlePackagesBuilder.VariantBuilder.jsLegacy() = js(legacyCompiler = true)
fun GradlePackagesBuilder.VariantBuilder.jsIr() = js(legacyCompiler = false)

fun GradlePackagesBuilder.VariantBuilder.category(category: String) =
    attribute("org.gradle.category", category)

fun GradlePackagesBuilder.VariantBuilder.libraryElements(elements: String) =
    attribute("org.gradle.libraryelements", elements)

fun GradlePackagesBuilder.VariantBuilder.usage(usage: String) =
    attribute("org.gradle.usage", usage)

fun GradlePackagesBuilder.VariantBuilder.libraryCategory() = category("library")
fun GradlePackagesBuilder.VariantBuilder.javaApi() = usage("java-api")
fun GradlePackagesBuilder.VariantBuilder.javaRuntime() = usage("java-runtime")
fun GradlePackagesBuilder.VariantBuilder.kotlinMetadata() {
    libraryCategory()
    usage("kotlin-metadata")
    kotlinPlatformType("common")
}
