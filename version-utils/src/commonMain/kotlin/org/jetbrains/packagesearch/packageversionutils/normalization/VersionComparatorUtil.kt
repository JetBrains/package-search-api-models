package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlin.math.sign

public object VersionComparatorUtil {
    private val VERSION_SPLITTER = Regex("(\\d+|[^\\d\\s()._\\-;:/, +~]+)")
    private val ZERO_PATTERN = Regex("0+")
    private val DIGITS_PATTERN = Regex("\\d+")
    private val DEFAULT_TOKEN_PRIORITY_PROVIDER: (String) -> Int =
        { param -> VersionTokenType.lookup(param).priority }

    public fun max(v1: String, v2: String): String = if (compare(v1, v2) > 0) v1 else v2
    public fun min(v1: String, v2: String): String = if (compare(v1, v2) < 0) v1 else v2

    private fun String.splitVersionString(): Sequence<String> =
        VERSION_SPLITTER.findAll(this).map { it.value }

    public fun compare(
        v1: String,
        v2: String,
        tokenPriorityProvider: (String) -> Int = DEFAULT_TOKEN_PRIORITY_PROVIDER,
    ): Int = v1.splitVersionString()
        .zip(v2.splitVersionString())
        .map { (e1, e2) ->
            val t1 = VersionTokenType.lookup(e1)
            val res = tokenPriorityProvider(e1) - tokenPriorityProvider(e2)
            when {
                res.sign != 0 -> res
                t1 == VersionTokenType.WORD -> e1.compareTo(e2)
                t1 == VersionTokenType.DIGITS -> compareNumbers(e1, e2)
                else -> 0
            }
        }
        .firstOrNull { it != 0 }
        ?: 0

    private fun compareNumbers(n1: String, n2: String): Int {
        val (num1, num2) = Pair(n1.trimStart('0'), n2.trimStart('0'))
        if (num1.isEmpty()) return if (num2.isEmpty()) 0 else -1
        if (num2.isEmpty()) return 1
        return compareBy<String> { it.length }.thenBy { it }.compare(num1, num2)
    }

    public enum class VersionTokenType(public val priority: Int) {
        SNAP(10),
        SNAPSHOT(10),
        S(10),
        DEV(10),
        DEVELOP(10),
        M(20),
        BUILD(20),
        MILESTONE(20),
        EAP(25),
        PRE(25),
        PREVIEW(25),
        ALPHA(30),
        A(30),
        BETA(40),
        BETTA(40),
        B(40),
        RC(50),
        WS(60),
        SP(70),
        REL(80),
        RELEASE(80),
        R(80),
        FINAL(80),
        CANDIDATE(80),
        STABLE(80),
        WORD(90),
        DIGITS(100),
        BUNDLED(666),
        SNAPSHOTS(10);

        public companion object {
            public fun lookup(str: String): VersionTokenType {
                val trimmedStr = str.trim()
                if (trimmedStr.isEmpty()) return WS
                for (token in values()) {
                    if (token.name[0] != '_' && token.name.equals(trimmedStr, ignoreCase = true)) {
                        return token
                    }
                }
                return when {
                    ZERO_PATTERN.matches(trimmedStr) -> WS
                    DIGITS_PATTERN.matches(trimmedStr) -> DIGITS
                    else -> WORD
                }
            }
        }
    }
}
