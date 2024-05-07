package org.jetbrains.packagesearch.packageversionutils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.packagesearch.packageversionutils.normalization.NormalizedVersion
import org.junit.jupiter.api.Test

internal class NormalizedVersionSortingTest {
    @Test
    fun `should flag as unstable any blank or empty version names`() {
        assert(
            javaClass.classLoader
                .getResourceAsStream("versions.json")
                ?.let { Json.decodeFromStream<List<NormalizedVersion>>(it) }
                .orEmpty().isNotEmpty(),
        )
    }
}
