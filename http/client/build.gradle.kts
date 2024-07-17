import org.gradle.kotlin.dsl.kotlin

plugins {
    `build-config`
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = "packagesearch-api-client" + artifactId.removePrefix(project.name)
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.packagesearchApiModels.http)
                api(packageSearchApiModelsVersions.ktor.client.content.negotiation)
                api(packageSearchApiModelsVersions.ktor.client.encoding)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx.json)
                api(packageSearchApiModelsVersions.ktor.serialization.kotlinx.protobuf)
                api(packageSearchApiModelsVersions.kotlinx.serialization.protobuf)
                api(packageSearchApiModelsVersions.kotlinx.serialization.json)
            }
        }
        commonTest{
            dependencies{
                api(kotlin("test-junit5"))
                api(packageSearchApiModelsVersions.kotlinx.coroutines.test)
                api(packageSearchApiModelsVersions.ktor.client.mock)
                api(packageSearchApiModelsVersions.ktor.client.logging)
                api(packageSearchApiModelsVersions.junit.jupiter.engine)
                api(packageSearchApiModelsVersions.mvstore)
                api(kotlinxDocumentStore.mvstore)
            }
        }
        jvmMain {
            dependencies {
                api(packageSearchApiModelsVersions.logback.classic)
            }
        }
    }
}