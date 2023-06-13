plugins {
    alias(packageSearchCatalog.plugins.kotlin.multiplatform)
    alias(packageSearchCatalog.plugins.kotlin.plugin.serialization)
    alias(packageSearchCatalog.plugins.detekt)
    alias(packageSearchCatalog.plugins.kotlinter)
    alias(packageSearchCatalog.plugins.packagesearch.build.config)
    `maven-publish`
}

group = "org.jetbrains.packagesearch"
version = System.getenv("GITHUB_REF")?.substringAfterLast("/") ?: "2.5.0"

dependencies {
    detektPlugins(packageSearchCatalog.logback.classic)
    detektPlugins(packageSearchCatalog.detekt.formatting)
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    ios()
    macosArm64()
    macosX64()
    watchos()
    tvos()

    sourceSets {
        commonMain {
            dependencies {
                implementation(packageSearchCatalog.kotlinx.serialization.json)
                implementation(packageSearchCatalog.kotlinx.datetime)
                api(packageSearchCatalog.packagesearch.version.utils)
            }
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            version = project.version.toString()
            groupId = group.toString()
            artifactId = project.name

            pom {
                name.set("Package Search - API models")
                description.set("API models for Package Search")
                url.set("https://package-search.jetbrains.com/")
                scm {
                    connection.set("scm:https://github.com/JetBrains/package-search-api-models.git")
                    developerConnection.set("scm:https://github.com/JetBrains/package-search-api-models.git")
                    url.set("https://github.com/JetBrains/package-search-api-models.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "Space"
            setUrl("https://packages.jetbrains.team/maven/p/kpm/public")
            credentials {
                username = System.getenv("MAVEN_SPACE_USERNAME")
                password = System.getenv("MAVEN_SPACE_PASSWORD")
            }
        }
    }
}

val isCi
    get() = System.getenv("CI") != null || System.getenv("CONTINUOUS_INTEGRATION") != null
