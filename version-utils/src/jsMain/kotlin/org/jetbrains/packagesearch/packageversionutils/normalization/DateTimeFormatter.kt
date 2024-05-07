package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.LocalDateTime
import kotlin.js.Date

@JsModule("date-fns")
@JsNonModule
@JsName("dateFns")
public external object DateFns {
    public fun parse(
        dateString: String,
        format: String,
        referenceDate: Date,
    ): Date
}

internal data class DateFnsFormat(val format: String)

public actual fun DateTimeFormatter(pattern: String): DateTimeFormatter = DateTimeFormatter(DateFnsFormat(pattern))

public actual class DateTimeFormatter internal constructor(private val format: DateFnsFormat) {
    public actual fun parse(dateTimeString: String): LocalDateTime {
        val date = DateFns.parse(dateTimeString, format.format, Date())
        return LocalDateTime(
            year = date.getFullYear(),
            monthNumber = date.getMonth(),
            dayOfMonth = date.getDate(),
            hour = date.getHours(),
            minute = date.getMinutes(),
            second = date.getSeconds(),
            nanosecond = date.getMilliseconds(),
        )
    }

    public actual fun parseOrNull(dateTimeString: String): LocalDateTime? = runCatching { parse(dateTimeString) }.getOrNull()
}
