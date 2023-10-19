package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
public class KotlinMultiplatformBuilder internal constructor(private val delegate: GradlePackagesBuilder) {

    internal fun metadata() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-metadata")
            kotlinPlatformType("common")
        }
    }

    public fun genericJvm(libraryElements: String? = null) {
        delegate.variant {
            jvm()
            libraryCategory()
            javaApi()
            libraryElements?.let { libraryElements(it) }
        }
        delegate.variant {
            jvm()
            libraryCategory()
            javaRuntime()
            libraryElements?.let { libraryElements(it) }
        }
    }

    public fun jvm() {
        genericJvm("jar")
    }
    public fun android() {
        genericJvm("aar")
    }

    public fun jsLegacy() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-api")
            jsLegacy()
        }
    }

    public fun jsIr() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-api")
            jsIr()
        }
    }

    public fun js(withLegacy: Boolean = true) {
        jsIr()
        if (withLegacy) jsLegacy()
    }

    public fun native(platform: String) {
        delegate.variant {
            native(platform)
            libraryCategory()
            usage("kotlin-api")
        }
    }
}

public fun GradlePackagesBuilder.kotlinMultiplatform(builder: KotlinMultiplatformBuilder.() -> Unit = {}) {
    KotlinMultiplatformBuilder(this)
        .apply { metadata() }
        .apply(builder)
}
