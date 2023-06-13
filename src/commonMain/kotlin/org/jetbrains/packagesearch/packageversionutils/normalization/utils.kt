package org.jetbrains.packagesearch.packageversionutils.normalization

fun NormalizedVersion.nonSemanticSuffixOrNull(): String? =
    when (this) {
        is NormalizedVersion.Semantic -> nonSemanticSuffix
        is NormalizedVersion.TimestampLike -> nonSemanticSuffix
        is NormalizedVersion.Garbage, is NormalizedVersion.Missing -> null
    }

// If only one of them has a releasedAt, it wins. If neither does, they're equal.
// If both have a releasedAt, we use those to discriminate.
fun NormalizedVersion.compareByTimestamp(other: NormalizedVersion): Int {
    return when {
        releasedAt == null && other.releasedAt != null -> -1
        releasedAt != null && other.releasedAt == null -> 1
        releasedAt != null && other.releasedAt != null -> releasedAt!!.compareTo(other.releasedAt!!)
        else -> 0
    }
}

private val HEX_STRING_LETTER_CHARS = 'a'..'f'

/**
 * Extracts stability markers. Must be used on the string that follows a valid semver (see [SEMVER_REGEX]).
 *
 * Stability markers are made up by a separator character (one of: . _ - +), then one of the stability tokens (see the list below), followed by an
 * optional separator (one of: . _ -), AND [0, 5] numeric digits. After the digits, there must be a word boundary (most punctuation, except for
 * underscores, qualifies as such).
 *
 * We only support up to two stability markers (arguably, having two already qualifies for the [Garbage] tier, but we have well-known libraries
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
 * Tokens denoted by a `*` are considered as meaningless words by [com.intellij.util.text.VersionComparatorUtil] when comparing without a custom
 * token priority provider, so sorting may be funky when they appear.
 */
private val STABILITY_MARKER_REGEX =
    ("^((?:[._\\-+]" +
            "(?:snapshots?|preview|milestone|candidate|release|develop|stable|build|alpha|betta|final|snap|beta|dev|pre|eap|rel|sp|rc|m|r|b|a|p)" +
            "(?:[._\\-]?\\d{1,5})?){1,2})(?:\\b|_)")
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


fun looksLikeGitCommitOrOtherHash(versionName: String): Boolean {
    val hexLookingPrefix = versionName.takeWhile { it.isDigit() || HEX_STRING_LETTER_CHARS.contains(it) }
    return when (hexLookingPrefix.length) {
        7, 40 -> true
        else -> false
    }
}

fun stabilitySuffixComponentOrNull(versionName: String, ignoredPrefix: String): String? {
    val groupValues = STABILITY_MARKER_REGEX.find(versionName.substringAfter(ignoredPrefix))
        ?.groupValues ?: return null
    if (groupValues.size <= 1) return null
    return groupValues[1].takeIf { it.isNotBlank() }
}

fun nonSemanticSuffix(versionName: String, ignoredPrefix: String?): String? {
    val semanticPart = stabilitySuffixComponentOrNull(versionName, ignoredPrefix ?: return null)
        ?: ignoredPrefix
    return versionName.substringAfter(semanticPart).takeIf { it.isNotBlank() }
}

fun isOneBigHexadecimalBlob(versionName: String): Boolean {
    var hasHexChars = false
    for (char in versionName.lowercase()) {
        when {
            char in HEX_STRING_LETTER_CHARS -> hasHexChars = true
            !char.isDigit() -> return false
        }
    }
    return hasHexChars
}

fun semanticVersionPrefixOrNull(versionName: String): String? {
    val groupValues = SEMVER_REGEX.find(versionName)?.groupValues ?: return null
    if (groupValues.size <= 1) return null
    return groupValues[1]
}

internal val NormalizedVersion.key
    get() = NormalizedVersionWeakCache.Key(versionName, releasedAt)

fun Int.signum(): Int {
    return when {
        this < 0 -> -1
        this > 0 -> 1
        else -> 0
    }
}