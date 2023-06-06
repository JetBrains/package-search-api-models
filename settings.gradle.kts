@file:Suppress("UnstableApiUsage")

rootProject.name = "package-search-api-models"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("packageSearchCatalog") {
            val catalogFile = file("../gradle/packagesearch.versions.toml")
            if (catalogFile.isFile) from(files(catalogFile))
            else from("org.jetbrains.packagesearch:packagesearch-version-catalog:1.0.0")
        }
    }
}