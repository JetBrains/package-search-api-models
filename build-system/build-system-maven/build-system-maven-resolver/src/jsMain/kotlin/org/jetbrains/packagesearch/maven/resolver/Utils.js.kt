package org.jetbrains.packagesearch.maven.resolver

import kotlin.js.Date
import kotlinx.datetime.Instant

public actual fun parseLastModifiedHeader(header: String): Instant? =
    Instant.fromEpochMilliseconds(Date(header).getTime().toLong())