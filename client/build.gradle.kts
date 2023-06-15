plugins {
    `build-config`
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-api-client"
        }
    }
}

kotlin {
    sourceSets {
        val ktorVersion = "2.3.1"
        commonMain {
            dependencies {
                api(projects.packagesearchApiModels)
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
        jsMain {
            dependencies {
                api("io.ktor:ktor-client-js:$ktorVersion")
            }
        }
        jvmMain {
            dependencies {
                api("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        appleMain {
            dependencies {
                api("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
    }
}