@file:Suppress("UnstableApiUsage")

plugins {
    `gradle-enterprise`
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "packagesearch-api-models"
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("packageSearchApiModelsVersions") {
            from(files("packagesearch-api-models.versions.toml"))
        }
    }
}


include(
    ":http",
    ":http:client",
    ":build-systems",
    ":version-catalog",
)

val isCi
    get() = System.getenv("CI") == "true"

gradleEnterprise {
    buildScan {
        server = "https://ge.labs.jb.gg/"
        accessKey = System.getenv("GRADLE_ENTERPRISE_ACCESS_KEY")
            ?: extra.properties["gradleEnterpriseAccessKey"]?.toString()
        publishAlwaysIf(isCi)
    }
}
