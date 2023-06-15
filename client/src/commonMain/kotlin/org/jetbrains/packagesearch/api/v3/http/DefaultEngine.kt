package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory

expect val DefaultEngine: HttpClientEngineFactory<HttpClientEngineConfig>
