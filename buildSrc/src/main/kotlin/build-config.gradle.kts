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
    jvm {
         jvmToolchain(11)
    }
    js(IR) {
        browser()
        nodejs()
    }
    ios()
    macosArm64()
    macosX64()
    watchosArm64()
    watchosX64()
    tvos()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
        val commonMain by getting
        val appleMain by creating {
            dependsOn(commonMain)
        }
        val watchosX64Main by getting {
            dependsOn(appleMain)
        }
        val watchosArm64Main by getting {
            dependsOn(appleMain)
        }
        val iosMain by getting {
            dependsOn(appleMain)
        }
        val macosMain by creating {
            dependsOn(appleMain)
        }
        val macosArm64Main by getting {
            dependsOn(macosMain)
        }
        val macosX64Main by getting {
            dependsOn(macosMain)
        }
        val tvosMain by getting {
            dependsOn(appleMain)
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
                name.set("Package Search - Version Utils")
                description.set("Utility to compare versions in Package Search")
                url.set("https://package-search.jetbrains.com/")
                scm {
                    connection.set("scm:https://github.com/JetBrains/package-search-version-utils.git")
                    developerConnection.set("scm:https://github.com/JetBrains/package-search-version-utils.git")
                    url.set("https://github.com/JetBrains/package-search-version-utils")
                }
            }
        }
    }
    repositories {
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