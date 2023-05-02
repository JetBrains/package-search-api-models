package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
class GradlePackagesBuilder {
    private val variants: MutableList<Map<String, String>> = mutableListOf()
    var isRootPublication: Boolean = true

    fun variant(attributes: Map<String, String>) {
        variants.add(attributes)
    }

    @SearchParametersBuilderDsl
    class VariantBuilder {
        private val attributes: MutableMap<String, String> = mutableMapOf()

        fun attribute(key: String, value: String) {
            attributes[key] = value
        }

        fun build() = attributes.toMap()
    }

    fun variant(block: VariantBuilder.() -> Unit) {
        variants.add(VariantBuilder().apply(block).build())
    }

    fun build() = GradlePackages(variants, isRootPublication)
}

fun GradlePackagesBuilder.VariantBuilder.kotlinPlatformType(platformType: String) =
    attribute("org.jetbrains.kotlin.platform.type", platformType)

fun GradlePackagesBuilder.VariantBuilder.kotlinNative(platform: String) {
    kotlinPlatformType("native")
    attribute("org.jetbrains.kotlin.native.target", platform)
}

fun GradlePackagesBuilder.VariantBuilder.kotlinJvm() = kotlinPlatformType("jvm")
fun GradlePackagesBuilder.VariantBuilder.kotlinJs(legacyCompiler: Boolean = false) {
    kotlinPlatformType("js")
    val compilerAttribute = if (legacyCompiler) "legacy" else "ir"
    attribute("org.jetbrains.kotlin.js.compiler", compilerAttribute)
}

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