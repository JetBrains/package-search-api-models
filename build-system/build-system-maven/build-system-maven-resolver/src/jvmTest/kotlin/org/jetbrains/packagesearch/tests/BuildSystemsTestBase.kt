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
}
