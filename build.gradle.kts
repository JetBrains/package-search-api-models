@file:Suppress("UNUSED_VARIABLE")

plugins {
    val kotlinVersion = "1.8.21"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jmailen.kotlinter") version "3.12.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
    `maven-publish`
}

group = "org.jetbrains.packagesearch"
version = "3.0.0"

val GITHUB_REF: String? = System.getenv("GITHUB_REF")
val devBranches = listOf("v3", "dev")
version = when {
    GITHUB_REF == null -> version
    GITHUB_REF.startsWith("refs/tags/") -> GITHUB_REF.substringAfter("refs/tags/")
    GITHUB_REF.startsWith("refs/heads") && devBranches.any { it in GITHUB_REF } ->
        "$version-SNAPSHOT"
    else -> version
}

GITHUB_REF?.let { logger.lifecycle("GITHUB_REF: $it") }

kotlin {
    jvm()
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
        val ktorVersion = "2.3.1"
        commonMain {
            dependencies {
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                api("com.soywiz.korlibs.krypto:krypto:4.0.5")
            }
        }
        val jsMain by getting {
            dependencies {
                api("io.ktor:ktor-client-js:$ktorVersion")
                api(npm("date-fns", "2.30.0"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                val junitVersion = "5.9.3"
                implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
                implementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")

                implementation("com.willowtreeapps.assertk:assertk:0.26.1")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            }
        }
        val appleMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api("io.ktor:ktor-client-cio:$ktorVersion")
            }
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