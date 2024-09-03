package org.jetbrains.packagesearch.maven

import kotlinx.datetime.Instant
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.timeIntervalSince1970

public actual fun parseLastModifiedHeader(header: String): Instant? =
    NSDateFormatter()
        .apply { dateFormat = "EEE, dd MMM yyyy HH:mm:ss zzz" }
        .apply { locale = NSLocale.localeWithLocaleIdentifier("en_US") }
        .dateFromString(header)
        ?.timeIntervalSince1970()
        ?.toLong()
        ?.let { Instant.fromEpochSeconds(it) }