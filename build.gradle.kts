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
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    `maven-publish`
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    id("org.jmailen.kotlinter") version "3.10.0"
    id("org.openapi.generator") version "6.0.0"
}

version = System.getenv("GITHUB_REF")?.substringAfterLast("/") ?: version

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.20.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
}

detekt {
    toolVersion = "1.20.0"
    autoCorrect = !isCi
    source = files("src/main/java", "src/main/kotlin")
    config = files("detekt.yml")
    buildUponDefaultConfig = true
}

kotlinter {
    reporters = arrayOf("html", "checkstyle", "plain")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            version = System.getenv("MAVEN_ARTIFACT_VERSION") ?: project.version as String

            from(components["java"])

            pom {
                name.set("Package Search - API models")
                description.set("API models for package search")
                url.set("https://package-search.jetbrains.com/")
                scm {
                    connection.set("scm:git:git://git.jetbrains.team/kpm/kpm.git")
                    developerConnection.set("scm:git:ssh://git.jetbrains.team/kpm/kpm.git")
                    url.set("https://jetbrains.team/p/kpm/code/kpm/")
                }
            }
        }
    }
    repositories {
        maven {
            name = "Space"
            setUrl(System.getenv("MAVEN_SPACE_URL"))
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

    packageName.set("com.jetbrains.packagesearch")
    apiPackage.set("com.jetbrains.packagesearch.api")
    invokerPackage.set("com.jetbrains.packagesearch.invoker")
    modelPackage.set("com.jetbrains.packagesearch.model")

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