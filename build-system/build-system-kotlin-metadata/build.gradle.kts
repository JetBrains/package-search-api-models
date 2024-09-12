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
    }
}