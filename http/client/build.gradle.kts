plugins {
    `build-config`
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-api-client" + artifactId.removePrefix(project.name)
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.packagesearchApiModels.http)
                api(packageSearchApiModelsVersions.ktor.client.content.negotiation)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx.json)
            }
        }
        jsMain {
            dependencies {
                api(packageSearchApiModelsVersions.ktor.client.js)
            }
        }
        jvmMain {
            dependencies {
                api(packageSearchApiModelsVersions.ktor.client.cio)
            }
        }
        appleMain {
            dependencies {
                api(packageSearchApiModelsVersions.ktor.client.cio)
            }
        }
    }
}