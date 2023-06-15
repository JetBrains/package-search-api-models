plugins {
    `kotlin-dsl`
}

dependencies {
    val kotlinVersion = "1.8.22"
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("org.jmailen.gradle:kotlinter-gradle:3.15.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.0")
}