plugins {
    id("build-config")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                api("com.soywiz.korlibs.krypto:krypto:4.0.5")
            }
        }
        jsMain{
            dependencies {
                api(npm("date-fns", "2.30.0"))
            }
        }
        jvmTest {
            dependencies {
                val junitVersion = "5.9.3"
                implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
                implementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")

                implementation("com.willowtreeapps.assertk:assertk:0.26.1")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            }
        }
    }
}