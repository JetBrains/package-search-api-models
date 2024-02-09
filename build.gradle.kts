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
    val kotlinVersion = "1.9.22"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    `maven-publish`
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    id("org.jmailen.kotlinter") version "4.2.0"
    id("org.openapi.generator") version "7.3.0"
}

group = "org.jetbrains.packagesearch"
version = System.getenv("GITHUB_REF")?.substringAfterLast("/") ?: "2.5.0"

dependencies {
    detektPlugins("ch.qos.logback:logback-classic:1.4.14")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
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

detekt {
    toolVersion = "1.20.0"
    autoCorrect = !isCi
    source.from(files("src/main/java", "src/main/kotlin"))
    config.from(files("detekt.yml"))
    buildUponDefaultConfig = true
}

kotlinter {
    reporters = arrayOf("html", "checkstyle", "plain")
}

val sourcesJar by tasks.registering(Jar::class) {
    group = "publishing"
    from(kotlin.sourceSets.main.get().kotlin.sourceDirectories)
    archiveClassifier = "sources"
    destinationDirectory = layout.buildDirectory.dir("artifacts")
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
                name = "Package Search - API models"
                description = "API models for Package Search"
                url = "https://package-search.jetbrains.com/"
                scm {
                    connection = "scm:https://github.com/JetBrains/package-search-api-models.git"
                    developerConnection = "scm:https://github.com/JetBrains/package-search-api-models.git"
                    url = "https://github.com/JetBrains/package-search-api-models.git"
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
        ?.let { inputSpec = it.second.absolutePath }
    outputDir = layout.buildDirectory.dir("generated/src/kotlin")
        .map { it.asFile.absolutePath }
    ignoreFileOverride = "$projectDir/.openapi-generator-ignore"

    generatorName = "kotlin"
    library = "multiplatform"
    generateApiDocumentation = false
    generateApiTests = false
    generateModelDocumentation = false
    generateModelTests = false

    packageName = "org.jetbrains.packagesearch"
    apiPackage = "org.jetbrains.packagesearch.api"
    invokerPackage = "org.jetbrains.packagesearch.invoker"
    modelPackage = "org.jetbrains.packagesearch.model"

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
        into(layout.buildDirectory.dir("generated/apiModels"))
    }
}

val isCi
    get() = System.getenv("CI") != null || System.getenv("CONTINUOUS_INTEGRATION") != null

object SemVer {
    fun parse(version: String, mode: SemverType = STRICT) = runCatching { Semver(version, mode) }.getOrNull()
}