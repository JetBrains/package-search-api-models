plugins {
    `build-config`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.versionUtils)
                api(packageSearchApiModelsVersions.datetime)
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
                api(packageSearchApiModelsVersions.krypto)
                api(kotlinxDocumentStore.core)
            }
        }
        jsMain {
            dependencies {
                api(npm(packageSearchApiModelsVersions.date.fns))
                api(kotlinxDocumentStore.browser)
            }
        }
        jvmMain{
            dependencies{
                api(kotlinxDocumentStore.mvstore)
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