plugins {
    `build-config`
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-sonatype-apis" + artifactId.removePrefix(project.name)
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
                api(packageSearchApiModelsVersions.ktor.client.content.negotiation)
                api(packageSearchApiModelsVersions.ktor.client.auth)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx.json)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx)
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
                api(packageSearchApiModelsVersions.ktor.client.cio)
            }
        }
    }
}