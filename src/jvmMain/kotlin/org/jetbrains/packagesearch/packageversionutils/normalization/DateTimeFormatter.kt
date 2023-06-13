package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.ChronoField
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter

actual fun DateTimeFormatter(pattern: String) =
    DateTimeFormatter(JavaDateTimeFormatter.ofPattern(pattern))

actual class DateTimeFormatter internal constructor(val delegate: JavaDateTimeFormatter) {
    actual fun parse(dateTimeString: String) =
        delegate.parse(dateTimeString)
            .getLong(ChronoField.INSTANT_SECONDS)
            .let { Instant.fromEpochSeconds(it) }
            .toLocalDateTime(TimeZone.currentSystemDefault())

    actual fun parseOrNull(dateTimeString: String) =
        runCatching { parse(dateTimeString) }.getOrNull()

}