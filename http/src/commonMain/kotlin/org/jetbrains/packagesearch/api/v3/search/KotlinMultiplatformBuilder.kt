package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
public class KotlinMultiplatformBuilder internal constructor(private val delegate: GradlePackagesBuilder) {
    internal fun metadata() {
        delegate.variant {
            kotlinPlatformType("common")
            libraryCategory()
            usage("kotlin-metadata")
            kotlinPlatformType("common")
        }
    }

    public fun jvm() {
        delegate.variant {
            libraryCategory()
            javaRuntime()
            libraryElements("jar")
        }
        delegate.variant {
            libraryCategory()
            javaApi()
            libraryElements("jar")
        }
    }

    public fun wasm() {
        delegate.variant {
            kotlinPlatformType("wasm")
            libraryCategory()
            usage("kotlin-api")
        }
    }

    public fun android() {
        delegate.variant {
            libraryCategory()
            javaRuntime()
        }
        delegate.variant {
            libraryCategory()
            javaApi()
        }
    }

    public fun jsLegacy() {
        delegate.variant {
            kotlinPlatformType("js")
            libraryCategory()
            usage("kotlin-api")
            attribute("org.jetbrains.kotlin.js.compiler", "legacy")
        }
    }

    public fun jsIr() {
        delegate.variant {
            kotlinPlatformType("js")
            libraryCategory()
            usage("kotlin-api")
            attribute("org.jetbrains.kotlin.js.compiler", "ir")
        }
    }

    public fun js(withLegacy: Boolean = true) {
        jsIr()
        if (withLegacy) jsLegacy()
    }

    public fun native(platform: String) {
        delegate.variant {
            kotlinPlatformType("native")
            attribute("org.jetbrains.kotlin.native.target", platform)
            libraryCategory()
            usage("kotlin-api")
        }
    }
}

@SearchParametersBuilderDsl
public fun GradlePackagesBuilder.kotlinMultiplatform(builder: KotlinMultiplatformBuilder.() -> Unit = {}) {
    KotlinMultiplatformBuilder(this).apply(builder)
}
