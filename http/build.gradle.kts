plugins {
    `build-config`
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-http-models" + artifactId.removePrefix(project.name)
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.packagesearchApiModels)
                api(packageSearchApiModelsVersions.ktor.http)
            }
        }
    }
}