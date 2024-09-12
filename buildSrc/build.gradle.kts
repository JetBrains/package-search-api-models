plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(packageSearchApiModelsVersions.kotlin.gradle.plugin)
    implementation(packageSearchApiModelsVersions.kotlin.serialization)
    implementation(packageSearchApiModelsVersions.kotlinter.gradle)
    implementation(packageSearchApiModelsVersions.detekt.gradle.plugin)
}