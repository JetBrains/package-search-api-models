package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

public actual val DefaultEngine: HttpClientEngineFactory<HttpClientEngineConfig>
    get() = Js