package org.jetbrains.packagesearch.maven

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.serialization
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XML
import org.jetbrains.packagesearch.BuildSystemsTestBase
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource


class Pom8Test : BuildSystemsTestBase() {

    val xml = XML {
        indentString = "    "
        defaultPolicy {
            ignoreUnknownChildren()
        }
    }

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            serialization(ContentType.Application.Xml, xml)
            serialization(ContentType.Text.Xml, xml)
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["maven/maven.xml", "maven/spring-core.xml", "maven/maven-core.xml"])
    fun `parse pom from resources`(path: String) = runTest {
        val pom = xml.decodeFromString<ProjectObjectModel>(readResourceAsText(path))
        println(xml.encodeToString(pom))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "org.apache.maven:maven:3.8.4",
            "org.springframework:spring-core:5.3.15",
            "com.fasterxml.jackson.core:jackson-databind:2.13.1",
            "org.hibernate:hibernate-core:5.6.7.Final",
            "org.apache.camel:camel-core:3.14.0",
            "org.apache.camel:camel-spring:3.14.0",
            "org.junit.jupiter:junit-jupiter-engine:5.9.1",
            "org.junit.jupiter:junit-jupiter-api:5.9.1",
            "org.junit.jupiter:junit-jupiter-params:5.9.1",
            "org.junit.platform:junit-platform-suite:1.9.1",
            "org.mockito:mockito-all:1.9.5",
            "org.mockito:mockito-core:3.12.4"
        ]
    )
    fun testSolver(coordinates: String) = runTest {
        val (groupId, artifactId, version) = coordinates.split(':')
        val pom = PomResolver(httpClient = httpClient).resolve(groupId, artifactId, version)
        println(xml.encodeToString(pom))
    }

}