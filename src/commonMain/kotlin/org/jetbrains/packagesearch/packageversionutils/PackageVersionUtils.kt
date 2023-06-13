package org.jetbrains.packagesearch.packageversionutils

object PackageVersionUtils {

    private val singleLetterUnstableMarkerRegex =
        "\\b[abmt][.\\-]?\\d{1,3}\\w?\\b".toRegex(RegexOption.IGNORE_CASE) // E.g., a01, b-2, m.3a

    /**
     * Evaluates if the given [versionName] may be considered a stable release or not.
     * @param versionName The version string that will be checked.
     * @param loggingCallback Invoked for logging purposes only when the [versionName]
     * is considered unstable with an explanation.
     * @return `true` if stable, `false` otherwise.
     */
    fun evaluateStability(versionName: String, loggingCallback: (String) -> Unit = {}): Boolean {
        if (versionName.isBlank()) return false

        if (singleLetterUnstableMarkerRegex.containsMatchIn(versionName)) {
            loggingCallback("Version '$versionName' contains a single-letter milestone/alpha/beta -> Unstable")
            return false
        }

        val tokens = tokenizeVersionName(versionName)

        return tokens.none { token ->
            VersionTokenMatcher.unstableTokens.any { matcher ->
                val matches = matcher.matches(token)
                if (matches) {
                    val detailMessage = when (matcher) {
                        is VersionTokenMatcher.SubstringMatcher -> "contains '${matcher.toMatch}'"
                        is VersionTokenMatcher.RegexMatcher -> "matches '${matcher.regex.pattern}'"
                    }
                    loggingCallback("Version '$versionName' $detailMessage -> Unstable")
                }
                matches
            }
        }
    }

    private fun tokenizeVersionName(versionName: String): List<String> {
        val tokens = mutableListOf<String>()

        var previousChar: Char? = null
        val tokenBuilder = StringBuilder(versionName.length)

        versionName.forEach { char ->
            @Suppress("UnsafeCallOnNullableType")
            if (previousChar != null && char.isTokenBoundary(previousChar!!)) {
                tokens += tokenBuilder.toString()
                tokenBuilder.clear()
            }
            tokenBuilder.append(char)
            previousChar = char
        }

        tokens += tokenBuilder.toString()

        return tokens.filter { token -> token.any { it.isLetterOrDigit() } }
    }

    private fun Char.isTokenBoundary(previousChar: Char): Boolean = when {
        !isLetterOrDigit() -> true
        isLetter() && !previousChar.isLetter() -> true
        isDigit() && !previousChar.isDigit() -> true
        else -> false
    }
}
