@file:Suppress("UnstableApiUsage")

rootProject.name = "packagesearch-api-models"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("packageSearchCatalog") {
            if (file("../packagesearch-version-catalog/packagesearch.versions.toml").exists())
                from(files("../packagesearch-version-catalog/packagesearch.versions.toml"))
            else from("org.jetbrains.packagesearch:packagesearch-version-catalog:1.0.0")
        }
    }
}