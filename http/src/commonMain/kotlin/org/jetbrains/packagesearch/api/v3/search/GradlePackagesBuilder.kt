package org.jetbrains.packagesearch.api.v3.search

import org.jetbrains.packagesearch.api.v3.ApiMavenPackage.GradleVersion.ApiVariant

@SearchParametersBuilderDsl
public class GradlePackagesBuilder internal constructor() {
    private val variants: MutableList<PackagesType.Gradle.Variant> = mutableListOf()

    @Deprecated("Use mustBeRootPublication instead", ReplaceWith("isRootPublication"))
    public var mustBeRootPublication: Boolean
        get() = isRootPublication
        set(value) {
            isRootPublication = value
        }

    public var isRootPublication: Boolean = true

    public fun variants(variants: List<PackagesType.Gradle.Variant>) {
        this.variants.addAll(variants)
    }

    @SearchParametersBuilderDsl
    public class VariantBuilder internal constructor() {
        private val attributes: MutableMap<String, ApiVariant.Attribute> = mutableMapOf()


        public var haveFiles: Boolean = false

        @Deprecated("Use mustBeWithFiles instead", ReplaceWith("haveFiles"))
        public var mustHaveFilesAttribute: Boolean
            get() = haveFiles
            set(value) {
                haveFiles = value
            }

        public fun attribute(key: String, value: String) {
            attributes[key] = ApiVariant.Attribute.create(key, value)
        }

        public fun attribute(key: String, value: ApiVariant.Attribute) {
            attributes[key] = value
        }

        public fun build(): PackagesType.Gradle.Variant =
            PackagesType.Gradle.Variant(attributes.toMap(), haveFiles)
    }

    public fun buildVariant(block: VariantBuilder.() -> Unit): PackagesType.Gradle.Variant =
        VariantBuilder().apply(block).build()

    public fun variant(block: VariantBuilder.() -> Unit) {
        variants.add(buildVariant(block))
    }

    internal fun build(): PackagesType.Gradle = PackagesType.Gradle(variants.toList(), isRootPublication)
}

public fun buildGradlePackages(block: GradlePackagesBuilder.() -> Unit): PackagesType.Gradle =
    GradlePackagesBuilder().apply(block).build()

public fun GradlePackagesBuilder.VariantBuilder.kotlinPlatformType(platformType: String) {
    attribute("org.jetbrains.kotlin.platform.type", platformType)
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

