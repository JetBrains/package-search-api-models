package org.jetbrains.packagesearch.api.v3.search

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

fun main() {
    val params = buildSearchParameters {
        onlyStable = true
        searchQuery = "ktor"

        gradlePackages {
            kotlinMultiplatform {
                jvm()
                js()
                native("ios_x64")
                native("linux_arm64")
                native("mingw_x64")
                native("macos_arm64")
                native("watchos_arm32")
                native("tvos_arm64")
            }
        }
        cocoapodsPackages {
            platform(CocoapodsPackages.Platform.IOS, "14.0")
        }
        mavenPackages()
        npmPackages()
    }
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    println(json.encodeToString(params))
}