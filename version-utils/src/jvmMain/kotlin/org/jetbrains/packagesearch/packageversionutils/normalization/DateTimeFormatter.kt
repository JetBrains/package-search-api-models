package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.ChronoField
import kotlinx.datetime.LocalDateTime
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter

public actual fun DateTimeFormatter(pattern: String): DateTimeFormatter =
    DateTimeFormatter(JavaDateTimeFormatter.ofPattern(pattern))

public actual class DateTimeFormatter internal constructor(public val delegate: JavaDateTimeFormatter) {
    public actual fun parse(dateTimeString: String): LocalDateTime =
        delegate.parse(dateTimeString)
            .getLong(ChronoField.INSTANT_SECONDS)
            .let { Instant.fromEpochSeconds(it) }
            .toLocalDateTime(TimeZone.currentSystemDefault())

    public actual fun parseOrNull(dateTimeString: String): LocalDateTime? =
        runCatching { parse(dateTimeString) }.getOrNull()
}
