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
                api(packageSearchApiModelsVersions.ktor.client.encoding)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx.json)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx.protobuf)
                api(packageSearchApiModelsVersions.kotlinx.serialization.protobuf)
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
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
                api(packageSearchApiModelsVersions.logback.classic)
            }
        }
        appleMain {
            dependencies {
                api(packageSearchApiModelsVersions.ktor.client.darwin)
            }
        }
    }
}