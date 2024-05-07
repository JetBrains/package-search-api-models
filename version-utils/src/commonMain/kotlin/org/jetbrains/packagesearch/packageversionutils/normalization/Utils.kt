package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

public fun NormalizedVersion.nonSemanticSuffixOrNull(): String? =
    when (this) {
        is NormalizedVersion.Semantic -> nonSemanticSuffix
        is NormalizedVersion.TimestampLike -> nonSemanticSuffix
        is NormalizedVersion.Garbage -> null
    }

// If only one of them has a releasedAt, it wins. If neither does, they're equal.
// If both have a releasedAt, we use those to discriminate.
public fun NormalizedVersion.compareByTimestamp(other: NormalizedVersion): Int {
    return when {
        releasedAt == null && other.releasedAt != null -> -1
        releasedAt != null && other.releasedAt == null -> 1
        releasedAt != null && other.releasedAt != null -> releasedAt!!.compareTo(other.releasedAt!!)
        else -> 0
    }
}

private val HEX_STRING_LETTER_CHARS: CharRange = 'a'..'f'

/**
 * Extracts stability markers. Must be used on the string that follows a valid semver (see [SEMVER_REGEX]).
 *
 * Stability markers are made up by a separator character (one of: . _ - +), then one of the stability tokens (see the list below), followed by an
 * optional separator (one of: . _ -), AND [0, 5] numeric digits. After the digits, there must be a word boundary (most punctuation, except for
 * underscores, qualifies as such).
 *
 * We only support up to two stability markers (arguably, but we have well-known libraries
 * out there that do the two-markers game, now and then, and we need to support those shenanigans).
 *
 * ### Stability tokens
 * We support the following stability tokens:
 * * `snapshots`*, `snapshot`, `snap`, `s`*
 * * `preview`, `eap`, `pre`, `p`*
 * * `develop`*, `dev`*
 * * `milestone`*, `m`, `build`*
 * * `alpha`, `a`
 * * `betta` (yes, there are Bettas out there), `beta`, `b`
 * * `candidate`*, `rc`
 * * `sp`
 * * `release`, `final`, `stable`*, `rel`, `r`
 *
 * Tokens denoted by a `*` are considered as meaningless words by [VersionComparatorUtil] when comparing without a custom
 * token priority provider, so sorting may be funky when they appear.
 */
private val STABILITY_MARKER_REGEX =
    (
        "^((?:[._\\-+]" +
            "(?:snapshots?|preview|milestone|candidate|release|develop|stable|build|alpha|betta|final|snap" +
            "|beta|dev|pre|eap|rel|sp|rc|m|r|b|a|p)" +
            "(?:[._\\-]?\\d{1,5})?){1,2})(?:\\b|_)"
    )
        .toRegex(option = RegexOption.IGNORE_CASE)

/**
 * Matches a whole string starting with a semantic version. A valid semantic version
 * has [1, 5] numeric components, each up to 5 digits long. Between each component
 * there is a period character.
 *
 * Examples of valid semver: 1, 1.0-whatever, 1.2.3, 2.3.3.0-beta02, 21.4.0.0.1
 * Examples of invalid semver: 1.0.0.0.0.1 (too many components), 123456 (component too long)
 *
 * Group 0 matches the whole string, group 1 is the semver minus any suffixes.
 */
private val SEMVER_REGEX = "^((?:\\d{1,5}\\.){0,4}\\d{1,5}(?!\\.?\\d)).*\$".toRegex(option = RegexOption.IGNORE_CASE)

public fun looksLikeGitCommitOrOtherHash(versionName: String): Boolean {
    val hexLookingPrefix = versionName.takeWhile { it.isDigit() || HEX_STRING_LETTER_CHARS.contains(it) }
    return when (hexLookingPrefix.length) {
        7, 40 -> true
        else -> false
    }
}

public fun stabilitySuffixComponentOrNull(
    versionName: String,
    ignoredPrefix: String,
): String? {
    val groupValues =
        STABILITY_MARKER_REGEX.find(versionName.substringAfter(ignoredPrefix))
            ?.groupValues ?: return null
    if (groupValues.size <= 1) return null
    return groupValues[1].takeIf { it.isNotBlank() }
}

public fun nonSemanticSuffix(
    versionName: String,
    ignoredPrefix: String?,
): String? {
    val semanticPart =
        stabilitySuffixComponentOrNull(versionName, ignoredPrefix ?: return null)
            ?: ignoredPrefix
    return versionName.substringAfter(semanticPart).takeIf { it.isNotBlank() }
}

public fun isOneBigHexadecimalBlob(versionName: String): Boolean {
    var hasHexChars = false
    for (char in versionName.lowercase()) {
        when {
            char in HEX_STRING_LETTER_CHARS -> hasHexChars = true
            !char.isDigit() -> return false
        }
    }
    return hasHexChars
}

public fun semanticVersionPrefixOrNull(versionName: String): String? {
    val groupValues = SEMVER_REGEX.find(versionName)?.groupValues ?: return null
    if (groupValues.size <= 1) return null
    return groupValues[1]
}

internal fun String.normalizedVersion(
    isStable: Boolean,
    releasedAt: Instant?,
    garbage: () -> NormalizedVersion.Garbage,
): NormalizedVersion {
    if (looksLikeGitCommitOrOtherHash(this) || isOneBigHexadecimalBlob(this)) {
        return garbage()
    }

    val timestampPrefix = VeryLenientDateTimeExtractor.extractTimestampLookingPrefixOrNull(this)
    if (timestampPrefix != null) {
        return NormalizedVersion.TimestampLike(
            versionName = this,
            isStable = isStable,
            releasedAt =
                VeryLenientDateTimeExtractor
                    .extractTimestampLookingPrefixOrNull(timestampPrefix)
                    ?.toInstant()
                    ?: releasedAt,
            timestampPrefix = timestampPrefix,
            stabilityMarker = stabilitySuffixComponentOrNull(this, timestampPrefix),
            nonSemanticSuffix = nonSemanticSuffix(this, timestampPrefix),
        )
    }

    val semanticPart = semanticVersionPrefixOrNull(this)
    if (semanticPart != null) {
        return NormalizedVersion.Semantic(
            versionName = this,
            isStable = isStable,
            releasedAt =
                VeryLenientDateTimeExtractor
                    .extractTimestampLookingPrefixOrNull(semanticPart)
                    ?.toInstant()
                    ?: releasedAt,
            semanticPart = semanticPart,
            stabilityMarker = stabilitySuffixComponentOrNull(this, semanticPart),
            nonSemanticSuffix = nonSemanticSuffix(this, semanticPart),
        )
    }

    return garbage()
}
