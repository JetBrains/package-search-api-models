plugins {
    `build-config`
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-build-systems-models" + artifactId.removePrefix(project.name)
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(packageSearchApiModelsVersions.xmlutil)
                api(packageSearchApiModelsVersions.kotlinx.serialization.core)
            }
        }
    }
}