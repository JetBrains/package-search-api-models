plugins {
    `build-config`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
                api(packageSearchApiModelsVersions.datetime)
                api(projects.core)
                api(projects.buildSystem.buildSystemGradle)
                api(projects.buildSystem.buildSystemMaven)
                api(projects.buildSystem.buildSystemKotlinMetadata)
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