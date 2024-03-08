package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.LocalDateTime

public expect fun DateTimeFormatter(pattern: String): DateTimeFormatter
public expect class DateTimeFormatter {
    public fun parse(dateTimeString: String): LocalDateTime
    public fun parseOrNull(dateTimeString: String): LocalDateTime?
}