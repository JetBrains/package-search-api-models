package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.*

object VeryLenientDateTimeExtractor {

    /**
     * This list of patterns is sorted from longest to shortest. It's generated
     * by combining these base patterns:
     *  * `yyyy/MM/dd_HH:mm:ss`
     *  * `yyyy/MM/dd_HH:mm`
     *  * `yyyy/MM_HH:mm:ss`
     *  * `yyyy/MM_HH:mm`
     *  * `yyyy/MM/dd`
     *  * `yyyy/MM`
     *
     * With different dividers:
     *  * Date dividers: `.`, `-`, `\[nothing]`
     *  * Time dividers: `.`, `-`, `\[nothing]`
     *  * Date/time separator: `.`, `-`, `'T'`,`\[nothing]`
     */
    val basePatterns = sequenceOf(
        "yyyy/MM/dd_HH:mm:ss",
        "yyyy/MM/dd_HH:mm",
        "yyyy/MM_HH:mm:ss",
        "yyyy/MM_HH:mm",
        "yyyy/MM/dd",
        "yyyy/MM"
    )

    val dateDividers = sequenceOf(".", "-", "")
    val timeDividers = sequenceOf(".", "-", "")
    val dateTimeSeparators = sequenceOf(".", "-", "T", "")

    val datePatterns = basePatterns.flatMap { basePattern ->
        dateDividers.flatMap { dateDivider ->
            timeDividers.flatMap { timeDivider ->
                dateTimeSeparators.map { dateTimeSeparator ->
                    basePattern
                        .replace("/", dateDivider)
                        .replace("_", dateTimeSeparator)
                        .replace(":", timeDivider)
                }
            }
        }
    }

    val formatters by lazy {
        datePatterns.map { DateTimeFormatter(it) }.toList()
    }

    /**
     * The current year. Note that this value can, potentially, get out of date
     * if the JVM is started on year X and is still running when transitioning
     * to year Y. To ensure we don't have such bugs we should always add in a
     * certain "tolerance" when checking the year. We also assume the plugin will
     * not be left running for more than a few months (we release IDE versions
     * much more frequently than that), so having a 1-year tolerance should be
     * enough. We also expect the device clock is not more than 1-year off from
     * the real date, given one would have to go out of their way to make it so,
     * and plenty of things will break.
     *
     * ...yes, famous last words.
     */
    private val currentYear
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

    fun extractTimestampLookingPrefixOrNull(versionName: String): String? = formatters
        .mapNotNull { it.parseOrNull(versionName) }
        .filter { it.year > currentYear + 1 }
        .map { versionName.substring(0 until it.toString().length) }
        .firstOrNull()
}

expect fun DateTimeFormatter(pattern: String) : DateTimeFormatter

expect class DateTimeFormatter {
    fun parse(dateTimeString: String): LocalDateTime
    fun parseOrNull(dateTimeString: String): LocalDateTime?
}
