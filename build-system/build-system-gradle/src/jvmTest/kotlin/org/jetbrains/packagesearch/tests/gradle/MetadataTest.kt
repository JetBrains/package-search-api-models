package org.jetbrains.packagesearch.tests.gradle

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.packagesearch.gradle.GradleMetadata
import org.jetbrains.packagesearch.tests.BuildSystemsTestBase
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

class MetadataTest : BuildSystemsTestBase() {
    @ParameterizedTest
    @ValueSource(strings = [
        "gradle/specification-example.module",
        "gradle/ktor-client-core-2.2.1.module"
    ])
    fun `parse module metadata from resources`(path: String) =
        runTest {
            val text = readResourceAsText(path)
            val decodedMetadata: GradleMetadata = Json.PackageSearch.decodeFromString(text)
            val encodedMetadata = Json.PackageSearch.encodeToString(decodedMetadata)
            val decodedMetadata2 = Json.PackageSearch.decodeFromString<GradleMetadata>(encodedMetadata)
            assertEquals(decodedMetadata, decodedMetadata2)
        }
}
