@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jmailen.kotlinter")
    id("io.gitlab.arturbosch.detekt")
    `maven-publish`
    id("version-config")
}

kotlin {
    explicitApi()
    jvm()
    jvmToolchain(17)
    js(IR) {
        browser()
        nodejs()
    }
    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs += "-Xexpect-actual-classes"
            }
        }
    }
    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.0")
    detektPlugins("ch.qos.logback:logback-classic:1.4.7")
}

val isCi
    get() = System.getenv("CI") != null || System.getenv("CONTINUOUS_INTEGRATION") != null

detekt {
    toolVersion = "1.23.0"
    autoCorrect = !isCi
    config.from("detekt.yml")
    buildUponDefaultConfig = true
}

kotlinter {
    reporters = arrayOf("html", "checkstyle", "plain")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name = "Package Search - Version Utils"
                description = "Utility to compare versions in Package Search"
                url = "https://package-search.jetbrains.com/"
                scm {
                    connection = "scm:https://github.com/JetBrains/package-search-version-utils.git"
                    developerConnection = "scm:https://github.com/JetBrains/package-search-version-utils.git"
                    url = "https://github.com/JetBrains/package-search-version-utils"
                }
            }
        }
    }
    repositories {
        maven(rootProject.layout.buildDirectory.dir("localMaven")) {
            name = "Local"
        }
        maven {
            name = "Space"
            setUrl("https://packages.jetbrains.team/maven/p/kpm/public")
            credentials {
                username = System.getenv("MAVEN_SPACE_USERNAME")
                password = System.getenv("MAVEN_SPACE_PASSWORD")
            }
        }
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

fun KotlinDependencyHandler.npm(dependencyProvider: Provider<MinimalExternalModuleDependency>): Dependency =
    npm(
        name = dependencyProvider.get().name,
        version = dependencyProvider.get().version ?: error("Version is required for ${dependencyProvider.get().name}")
    )