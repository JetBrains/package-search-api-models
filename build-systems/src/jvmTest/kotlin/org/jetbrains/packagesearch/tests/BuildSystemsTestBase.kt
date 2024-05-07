package org.jetbrains.packagesearch.tests

import kotlinx.serialization.json.Json

abstract class BuildSystemsTestBase {
    companion object {
        fun readResourceAsText(path: String) =
            BuildSystemsTestBase::class.java
                .classLoader
                .getResource(path)
                ?.readText()
                ?: error("Resource '$path' not found")
    }

    // Keep in sync with `org.jetbrains.packagesearch.backend.utils.JsonKt`
    @Suppress("UnusedReceiverParameter", "PropertyName")
    protected val Json.PackageSearch
        get() =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = false
                isLenient = false
            }
}
