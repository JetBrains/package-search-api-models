package org.jetbrains.packagesearch.api.v3.http

import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

public fun buildUrl(action: URLBuilder.() -> Unit): Url = URLBuilder().apply(action).build()

internal fun HttpMessageBuilder.header(key: String, vararg values: Any?) {
    headers.append(key, values.joinToString())
}

internal var HttpTimeout.HttpTimeoutCapabilityConfiguration.requestTimeout: Duration?
    get() = requestTimeoutMillis?.milliseconds
    set(value) { requestTimeoutMillis = value?.inWholeMilliseconds }

internal fun HttpRequestRetry.Configuration.constantDelay(
    delay: Duration = 1.seconds,
    randomization: Duration = 1.seconds,
    respectRetryAfterHeader: Boolean = true
) {
    constantDelay(delay.inWholeMilliseconds, randomization.inWholeMilliseconds, respectRetryAfterHeader)
}