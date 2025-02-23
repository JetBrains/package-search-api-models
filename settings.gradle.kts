@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
    `gradle-enterprise`
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "packagesearch-api-models"
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/kpm/public")
        gradlePluginPortal()
    }
    versionCatalogs {
        create("packageSearchApiModelsVersions") {
            from(files("packagesearch-api-models.versions.toml"))
        }
        create("kotlinxDocumentStore") {
            from("com.github.lamba92:kotlinx-document-store-version-catalog:0.0.3")
        }
    }
}


include(
    ":http",
    ":sonatype-apis",
    ":version-utils",
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
