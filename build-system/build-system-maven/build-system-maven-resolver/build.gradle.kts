plugins {
    `build-config`
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-build-systems-models" + artifactId.removePrefix(project.name)
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.buildSystem.buildSystemMaven)
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
                api(packageSearchApiModelsVersions.ktor.client.content.negotiation)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx)
                api(packageSearchApiModelsVersions.datetime)
                api(packageSearchApiModelsVersions.krypto)
            }
        }
        jvmMain {
            dependencies {
                api(packageSearchApiModelsVersions.ktor.client.cio)
            }
        }
        jsMain {
            dependencies {
                api(packageSearchApiModelsVersions.ktor.client.js)
            }
        }
        appleMain {
            dependencies {
                api(packageSearchApiModelsVersions.ktor.client.darwin)
            }
        }
        jvmTest {
            dependencies {
                implementation(packageSearchApiModelsVersions.kotlinx.coroutines.test)
                api(packageSearchApiModelsVersions.ktor.client.logging)
                implementation(kotlin("test-junit5"))
                implementation(packageSearchApiModelsVersions.junit.jupiter.api)
                implementation(packageSearchApiModelsVersions.junit.jupiter.params)
                runtimeOnly(packageSearchApiModelsVersions.junit.jupiter.engine)
                runtimeOnly(packageSearchApiModelsVersions.logback.classic)
            }
        }
    }
}