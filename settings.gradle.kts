@file:Suppress("UnstableApiUsage")


enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "packagesearch-api-models"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":client")