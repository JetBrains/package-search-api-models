package org.jetbrains.packagesearch.maven

import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.time.Instant as JavaInstant

public actual fun parseLastModifiedHeader(header: String): Instant? =
    runCatching {
        RFC_1123_DATE_TIME.parse(header)
            .let { JavaInstant.from(it) }
            .toKotlinInstant()
    }.getOrNull()