package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

actual val DefaultEngine: HttpClientEngineFactory<HttpClientEngineConfig>
    get() = Js