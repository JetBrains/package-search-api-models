package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.engine.*

expect val DefaultEngine : HttpClientEngineFactory<HttpClientEngineConfig>