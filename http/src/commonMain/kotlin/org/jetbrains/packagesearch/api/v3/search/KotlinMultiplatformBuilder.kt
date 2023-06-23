package org.jetbrains.packagesearch.api.v3.search

@SearchParametersBuilderDsl
class KotlinMultiplatformBuilder internal constructor(private val delegate: GradlePackagesBuilder) {

    fun genericJvm(libraryElements: String? = null) {
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

    fun jvm() = genericJvm("jar")
    fun android() = genericJvm("aar")

    fun jsLegacy() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-api")
            jsLegacy()
        }
    }

    fun jsIr() {
        delegate.variant {
            libraryCategory()
            usage("kotlin-api")
            jsIr()
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
