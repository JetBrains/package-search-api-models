@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("com.gradle.develocity") version "3.17"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "packagesearch-api-models"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/kpm/public")
    }
    versionCatalogs {
        create("packageSearchApiModelsVersions") {
            from(files("packagesearch-api-models.versions.toml"))
        }
        create("kotlinxDocumentStore") {
            from("com.github.lamba92:kotlinx-document-store-version-catalog:1.0.0-SNAPSHOT")
        }
    }
}

include(
    ":http",
    ":sonatype-apis",
    ":version-utils",
    ":http:client",
    ":build-system:build-system-kotlin-metadata",
    ":build-system:build-system-maven",
    ":build-system:build-system-maven:build-system-maven-resolver",
    ":build-system:build-system-gradle",
    ":version-catalog",
)

val isCi
    get() = System.getenv("CI") == "true"

develocity {
    server = "https://ge.labs.jb.gg/"
    accessKey = System.getenv("GRADLE_ENTERPRISE_ACCESS_KEY")
        ?: extra.properties["gradleEnterpriseAccessKey"]?.toString()
    buildScan {
        publishing.onlyIf { isCi }
    }
}
