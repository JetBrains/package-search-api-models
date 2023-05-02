package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
class KotlinMultiplatformBuilder internal constructor(private val delegate: GradlePackagesBuilder) {

    fun jvm() {
        delegate.variant {
            jvm()
            libraryCategory()
            javaApi()
            libraryElements("jar")
        }
        delegate.variant {
            jvm()
            libraryCategory()
            javaRuntime()
            libraryElements("jar")
        }
    }

    fun jsLegacy() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-api")
            js(true)
        }
    }
    fun jsIr() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-api")
            js(false)
        }
    }

    fun js(withLegacy: Boolean = true) {
        jsIr()
        if (withLegacy) jsLegacy()
    }

    fun native(platform: String) {
        delegate.variant {
            native(platform)
            libraryCategory()
            usage("kotlin-api")
        }
    }
}

fun GradlePackagesBuilder.kotlinMultiplatform(builder: KotlinMultiplatformBuilder.() -> Unit) {
    KotlinMultiplatformBuilder(this).apply(builder)
}