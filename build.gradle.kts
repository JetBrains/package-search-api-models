plugins {
    `build-config`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
                api(projects.buildSystem.buildSystemMaven)
                api(projects.buildSystem.buildSystemGradle)
                api(projects.buildSystem.buildSystemKotlinMetadata)
                api(projects.versionUtils)
                api(packageSearchApiModelsVersions.datetime)
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
                api(packageSearchApiModelsVersions.krypto)
            }
        }
        jsMain {
            dependencies {
                api(npm(packageSearchApiModelsVersions.date.fns))
            }
        }
        jvmTest {
            dependencies {
                implementation(packageSearchApiModelsVersions.junit.jupiter.api)
                implementation(packageSearchApiModelsVersions.junit.jupiter.params)
                implementation(packageSearchApiModelsVersions.assertk)
                runtimeOnly(packageSearchApiModelsVersions.junit.jupiter.engine)
            }
        }
    }
}