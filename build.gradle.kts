import com.vdurmont.semver4j.Semver
import com.vdurmont.semver4j.Semver.SemverType
import com.vdurmont.semver4j.Semver.SemverType.STRICT

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.vdurmont:semver4j:3.1.0")
    }
}

plugins {
    val kotlinVersion = "1.8.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    `maven-publish`
    //id("io.gitlab.arturbosch.detekt") version "1.20.0"
    //id("org.jmailen.kotlinter") version "3.10.0"
    id("org.openapi.generator") version "6.0.0"
}

group = "org.jetbrains.packagesearch"
version = System.getenv("GITHUB_REF")?.substringAfterLast("/") ?: "2.5.0"

dependencies {
//    detektPlugins("ch.qos.logback:logback-classic:1.2.11")
//    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.20.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
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

openApiGenerate {

    file("swagger").listFiles()
        ?.filter { it.extension.contains("yaml") }
        ?.mapNotNull { file -> SemVer.parse(file.nameWithoutExtension)?.let { semver -> semver to file } }
        ?.maxByOrNull { it.first }
        ?.let { inputSpec.set(it.second.absolutePath) }

    outputDir.set("$buildDir/generates/src/kotlin")
    ignoreFileOverride.set("$projectDir/.openapi-generator-ignore")

    generatorName.set("kotlin")
    library.set("multiplatform")
    generateApiDocumentation.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateModelTests.set(false)

    packageName.set("org.jetbrains.packagesearch")
    apiPackage.set("org.jetbrains.packagesearch.api")
    invokerPackage.set("org.jetbrains.packagesearch.invoker")
    modelPackage.set("org.jetbrains.packagesearch.model")

    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "enumPropertyNaming" to "UPPERCASE",
            "collectionType" to "list"
        )
    )
}

//TODO: need to refactoring this idea when we start working on v3 API
// See more details here: https://youtrack.jetbrains.com/issue/PKGS-695#focus=Comments-27-4906671.0-0
tasks {
    openApiGenerate.configure {
        outputs.dir(outputDir)
    }
    register<Sync>("copyOpenApiGeneratedModels") {
        dependsOn(openApiGenerate)
        from(openApiGenerate) {
            include("**/model/**")
            includeEmptyDirs = false
        }
        into("$buildDir/generated/apiModels")
    }
}

val isCi
    get() = System.getenv("CI") != null || System.getenv("CONTINUOUS_INTEGRATION") != null

object SemVer {
    fun parse(version: String, mode: SemverType = STRICT) = runCatching { Semver(version, mode) }.getOrNull()
}