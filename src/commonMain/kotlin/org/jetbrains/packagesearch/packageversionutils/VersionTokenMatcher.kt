package org.jetbrains.packagesearch.packageversionutils

sealed class VersionTokenMatcher {

    abstract fun matches(value: String): Boolean

    class SubstringMatcher(val toMatch: String) : VersionTokenMatcher() {

        private val toMatchLength = toMatch.length

        override fun matches(value: String): Boolean {
            val substringIndex = value.indexOf(toMatch, ignoreCase = true)
            if (substringIndex < 0) return false

            val afterSubstringIndex = substringIndex + toMatchLength
            val valueLength = value.length

            // Case 1. The value matches entirely
            if (substringIndex == 0 && afterSubstringIndex == valueLength) return true

            // Case 2. The match is at the beginning of value
            if (substringIndex == 0) {
                val nextLetter = value[afterSubstringIndex]
                return !nextLetter.isLetter() // Matching whole word
            }

            // Case 2. The match is at the end of value
            if (afterSubstringIndex == valueLength) {
                val previousLetter = value[substringIndex - 1]
                return !previousLetter.isLetterOrDigit() && previousLetter != '_' // Matching whole word
            }

            // Case 3. The match is somewhere inside of value
            val previousLetter = value[substringIndex - 1]
            val startsAtWordBoundary = !previousLetter.isLetterOrDigit() && previousLetter != '_'
            val nextLetter = value[afterSubstringIndex]
            val endsAtWordBoundary = !nextLetter.isLetter()

            return startsAtWordBoundary && endsAtWordBoundary // Needs to be matching a whole word
        }
    }

    class RegexMatcher(val regex: Regex) : VersionTokenMatcher() {

        override fun matches(value: String): Boolean = regex.containsMatchIn(value)
    }

    companion object {

        fun substring(toMatch: String) = SubstringMatcher(toMatch)

        fun regex(regex: Regex) = RegexMatcher(regex)

        val unstableTokens
            get() = listOf(
                substring("alpha"),
                substring("beta"),
                substring("bate"),
                substring("commit"),
                substring("unofficial"),
                substring("exp"),
                substring("experiment"),
                substring("experimental"),
                substring("milestone"),
                substring("deprecated"),
                substring("rc"),
                substring("rctest"),
                substring("cr"),
                substring("draft"),
                substring("ignored"),
                substring("test"),
                substring("placeholder"),
                substring("incubating"),
                substring("nightly"),
                substring("weekly"),
                substring("master"),
                substring("main"),
                regex("\\b(rel(ease)?[.\\-_]?)?candidate\\b".toRegex(RegexOption.IGNORE_CASE)),
                regex("\\br?dev(elop(ment)?)?\\b".toRegex(RegexOption.IGNORE_CASE)),
                regex("\\beap?\\b".toRegex(RegexOption.IGNORE_CASE)),
                regex("pre(view)?\\b".toRegex(RegexOption.IGNORE_CASE)),
                regex("\\bsnap(s?shot)?\\b".toRegex(RegexOption.IGNORE_CASE))
            )
    }
}
