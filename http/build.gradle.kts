plugins {
    `build-config`
    application
}

application {
    mainClass = "org.jetbrains.packagesearch.api.v3.search.SearchParametersBuilderKt"
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