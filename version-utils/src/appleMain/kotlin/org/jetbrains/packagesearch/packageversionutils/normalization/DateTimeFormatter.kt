package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

public fun NSDateFormatter(pattern: String): NSDateFormatter =
    NSDateFormatter().apply { dateFormat = pattern }

public actual fun DateTimeFormatter(pattern: String): DateTimeFormatter =
    DateTimeFormatter(NSDateFormatter(pattern))

public actual class DateTimeFormatter internal constructor(private val delegate: NSDateFormatter) {

    public actual fun parseOrNull(dateTimeString: String): LocalDateTime? = delegate.dateFromString(dateTimeString)
        ?.timeIntervalSince1970
        ?.toLong()
        ?.let { Instant.fromEpochSeconds(it, 0) }
        ?.toLocalDateTime(TimeZone.currentSystemDefault())

    public actual fun parse(dateTimeString: String): LocalDateTime =
        parseOrNull(dateTimeString)
            ?: error("Unable to parse '$dateTimeString' with pattern '${delegate.dateFormat}'")
}