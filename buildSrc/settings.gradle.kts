@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("packageSearchApiModelsVersions") {
            from(files("../packagesearch-api-models.toml"))
        }
    }
}