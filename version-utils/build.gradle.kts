plugins {
    `build-config`
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-version-utils" + artifactId.removePrefix(project.name)
        }
    }
}

kotlin {
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.experimental.ExperimentalNativeApi")
            }
        }
        commonMain {
            dependencies {
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