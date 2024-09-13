import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    `build-config`
}

val generated = layout.buildDirectory.dir("generated/commonMain/kotlin")

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDirs(generated)
            dependencies {
                api(projects.versionUtils)
                api(packageSearchApiModelsVersions.datetime)
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
                api(packageSearchApiModelsVersions.krypto)
            }
        }
        jsMain {
            dependencies {
                api(npm(packageSearchApiModelsVersions.date.fns))
                api(kotlinxDocumentStore.browser)
            }
        }
        jvmMain{
            dependencies{
                api(kotlinxDocumentStore.mvstore)
            }
        }
        jvmTest {
            dependencies {
                implementation(packageSearchApiModelsVersions.junit.jupiter.api)
                implementation(packageSearchApiModelsVersions.junit.jupiter.params)
                implementation(packageSearchApiModelsVersions.assertk)
                runtimeOnly(packageSearchApiModelsVersions.junit.jupiter.engine)
            }
        }
    }
}

tasks {
    val generateApiClientObject by registering(GenerateApiClientObject::class) {
        group = "generate"
        outputDir = generated
        packageName = "org.jetbrains.packagesearch.api"
    }
    compileKotlinJs {
        dependsOn(generateApiClientObject)
    }
    jsSourcesJar{
        dependsOn(generateApiClientObject)
    }
    jvmSourcesJar{
        dependsOn(generateApiClientObject)
    }
    sourcesJar{
        dependsOn(generateApiClientObject)
    }
    withType<KotlinCompileCommon> {
        dependsOn(generateApiClientObject)
    }
    withType<SourceTask>{
        dependsOn(generateApiClientObject)
    }
    withType<KotlinCompile> {
        dependsOn(generateApiClientObject)
    }
    withType<LintTask> {
        dependsOn(generateApiClientObject)
    }
    withType<FormatTask>{
        dependsOn(generateApiClientObject)
    }
}