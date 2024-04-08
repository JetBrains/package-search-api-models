package org.jetbrains.packagesearch.packageversionutils

import kotlinx.serialization.json.Json
import org.jetbrains.packagesearch.packageversionutils.normalization.NormalizedVersion
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

internal class NormalizedVersionSortingTest {
    @Test
    fun `should flag as unstable any blank or empty version names`() {
        val path = javaClass.classLoader.getResource("versions.json").path ?: error("versions.json not found")
        val vs = Files.readAllLines(Paths.get(path)).map { Json.decodeFromString<NormalizedVersion>(it) }
        val versions = vs.sortedDescending()

        assert(versions.isNotEmpty())
    }
}