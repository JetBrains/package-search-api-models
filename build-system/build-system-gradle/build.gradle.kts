plugins {
    `build-config`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
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