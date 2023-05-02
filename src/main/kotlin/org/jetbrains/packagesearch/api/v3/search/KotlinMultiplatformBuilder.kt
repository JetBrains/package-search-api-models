package org.jetbrains.packagesearch.api.v3.search

class KotlinMultiplatformBuilder internal constructor(private val delegate: GradlePackagesBuilder) {
    fun kotlinJvm() {
        delegate.variant {
            kotlinJvm()
            libraryCategory()
            javaApi()
            libraryElements("jar")
        }
        delegate.variant {
            kotlinJvm()
            libraryCategory()
            javaRuntime()
            libraryElements("jar")
        }
    }

    fun kotlinJsLegacy() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-api")
            kotlinJs(true)
        }
    }
    fun kotlinJsIr() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-api")
            kotlinJs(false)
        }
    }

    fun kotlinJs(withLegacy: Boolean = true) {
        kotlinJsIr()
        if (withLegacy) kotlinJsLegacy()
    }

    fun kotlinNative(platform: String) {
        delegate.variant {
            kotlinNative(platform)
            libraryCategory()
            usage("kotlin-api")
        }
    }
}

fun GradlePackagesBuilder.kotlinMultiplatform(builder: KotlinMultiplatformBuilder.() -> Unit) {
    KotlinMultiplatformBuilder(this).apply(builder)
}