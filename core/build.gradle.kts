plugins {
    `build-config`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(packageSearchApiModelsVersions.kotlinx.serialization.core)
                api(projects.versionUtils)
            }
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-api-models-" + artifactId.removePrefix(project.name)
        }
    }
}