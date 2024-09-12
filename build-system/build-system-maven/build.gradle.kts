plugins {
    `build-config`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(packageSearchApiModelsVersions.xmlutil)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx)
                api(packageSearchApiModelsVersions.datetime)
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