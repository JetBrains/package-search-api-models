package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.LocalDateTime
import kotlin.js.Date

@JsModule("date-fns")
@JsNonModule
@JsName("dateFns")
external object DateFns {
    fun parse(dateString: String, format: String, referenceDate: Date): Date
}

internal data class DateFnsFormat(val format: String)

actual fun DateTimeFormatter(pattern: String) =
    DateTimeFormatter(DateFnsFormat(pattern))

actual class DateTimeFormatter internal constructor(private val format: DateFnsFormat) {
        actual fun parse(dateTimeString: String): LocalDateTime {
            val date = DateFns.parse(dateTimeString, format.format, Date())
            return LocalDateTime(
                year = date.getFullYear(),
                monthNumber = date.getMonth(),
                dayOfMonth = date.getDate(),
                hour = date.getHours(),
                minute = date.getMinutes(),
                second = date.getSeconds(),
                nanosecond = date.getMilliseconds()
            )
        }

        actual fun parseOrNull(dateTimeString: String) =
            runCatching { parse(dateTimeString) }.getOrNull()
    }
