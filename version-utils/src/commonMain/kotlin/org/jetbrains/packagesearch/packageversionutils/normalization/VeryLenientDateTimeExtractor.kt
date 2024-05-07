package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public object VeryLenientDateTimeExtractor {
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
    public val basePatterns: Sequence<String> =
        sequenceOf(
            "yyyy/MM/dd_HH:mm:ss",
            "yyyy/MM/dd_HH:mm",
            "yyyy/MM_HH:mm:ss",
            "yyyy/MM_HH:mm",
            "yyyy/MM/dd",
            "yyyy/MM",
        )

    public val dateDividers: Sequence<String> = sequenceOf(".", "-", "")
    public val timeDividers: Sequence<String> = sequenceOf(".", "-", "")
    public val dateTimeSeparators: Sequence<String> = sequenceOf(".", "-", "'T'", "")

    public val datePatterns: Sequence<String> =
        basePatterns.flatMap { basePattern ->
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

    public val formatters: List<DateTimeFormatter> by lazy {
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

    public fun extractTimestampLookingPrefixOrNull(versionName: String): String? =
        formatters
            .mapNotNull { it.parseOrNull(versionName) }
            .filter { it.year > currentYear + 1 }
            .map { versionName.substring(0 until it.toString().length) }
            .firstOrNull()
}
