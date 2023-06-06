plugins {
    alias(packageSearchCatalog.plugins.kotlin.jvm)
    alias(packageSearchCatalog.plugins.kotlin.plugin.serialization)
    `maven-publish`
//    alias(packageSearchCatalog.plugins.detekt)
//    alias(packageSearchCatalog.plugins.kotlinter)
}

group = "org.jetbrains.packagesearch"
version = System.getenv("GITHUB_REF")?.substringAfterLast("/") ?: "2.5.0"

dependencies {
//    detektPlugins(packageSearchCatalog.logback.classic)
//    detektPlugins(packageSearchCatalog.detekt.formatting)
    implementation(packageSearchCatalog.kotlinx.serialization.json)
    implementation(packageSearchCatalog.kotlinx.datetime)
    api(packageSearchCatalog.packagesearch.version.utils)
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}

//detekt {
//    toolVersion = "1.20.0"
//    autoCorrect = !isCi
//    source = files("src/main/java", "src/main/kotlin")
//    config = files("detekt.yml")
//    buildUponDefaultConfig = true
//}

//kotlinter {
//    reporters = arrayOf("html", "checkstyle", "plain")
//}

val sourcesJar by tasks.registering(Jar::class) {
    group = "publishing"
    from(kotlin.sourceSets.main.get().kotlin.sourceDirectories)
    archiveClassifier.set("sources")
    destinationDirectory.set(buildDir.resolve("artifacts"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar)

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
