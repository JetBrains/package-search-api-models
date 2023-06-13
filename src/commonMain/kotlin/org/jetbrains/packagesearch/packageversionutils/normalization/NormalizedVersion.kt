package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.packageversionutils.PackageVersionUtils

@Serializable
sealed interface NormalizedVersion : Comparable<NormalizedVersion> {

    companion object {

        fun from(versionName: String?, releasedAt: Instant? = null): NormalizedVersion {
            if (versionName.isNullOrBlank()) return Missing
            return NormalizedVersionWeakCache.getOrPut(versionName, releasedAt) {
                normalizePackageVersion(
                    versionName = versionName,
                    isStable = PackageVersionUtils.evaluateStability(versionName)
                ) {
                    Garbage(
                        versionName = versionName,
                        isStable = PackageVersionUtils.evaluateStability(versionName),
                        releasedAt = releasedAt
                    )
                }
            }
        }

        private fun normalizePackageVersion(
            versionName: String,
            isStable: Boolean,
            garbage: () -> Garbage,
        ): NormalizedVersion {
            if (looksLikeGitCommitOrOtherHash(versionName) || isOneBigHexadecimalBlob(versionName)) {
                return garbage()
            }

            val timestampPrefix = VeryLenientDateTimeExtractor.extractTimestampLookingPrefixOrNull(versionName)
            if (timestampPrefix != null) {
                return TimestampLike(
                    versionName = versionName,
                    isStable = isStable,
                    releasedAt = VeryLenientDateTimeExtractor
                        .extractTimestampLookingPrefixOrNull(timestampPrefix)
                        ?.toInstant(),
                    timestampPrefix = timestampPrefix,
                    stabilityMarker = stabilitySuffixComponentOrNull(versionName, timestampPrefix),
                    nonSemanticSuffix = nonSemanticSuffix(versionName, timestampPrefix)
                )
            }

            val semanticPart = semanticVersionPrefixOrNull(versionName)
            if (semanticPart != null) {
                return Semantic(
                    versionName = versionName,
                    isStable = isStable,
                    releasedAt = VeryLenientDateTimeExtractor
                        .extractTimestampLookingPrefixOrNull(semanticPart)
                        ?.toInstant(),
                    semanticPart = semanticPart,
                    stabilityMarker = stabilitySuffixComponentOrNull(versionName, semanticPart),
                    nonSemanticSuffix = nonSemanticSuffix(versionName, semanticPart)
                )
            }

            return garbage()
        }
    }

    val versionName: String
    val isStable: Boolean
    val releasedAt: Instant?

    @Serializable
    @SerialName("semantic")
    data class Semantic(
        override val versionName: String,
        override val isStable: Boolean,
        override val releasedAt: Instant?,
        val semanticPart: String,
        override val stabilityMarker: String?,
        override val nonSemanticSuffix: String?,
    ) : NormalizedVersion, DecoratedVersion {

        val semanticPartWithStabilityMarker
            get() = semanticPart + (stabilityMarker ?: "")

        override fun compareTo(other: NormalizedVersion): Int =
            when (other) {
                is Semantic -> compareByNameAndThenByTimestamp(other)
                is TimestampLike, is Garbage, is Missing -> 1
            }

        private fun compareByNameAndThenByTimestamp(other: Semantic): Int {
            // First, compare semantic parts and stability markers only
            val nameComparisonResult = VersionComparatorUtil.compare(
                semanticPartWithStabilityMarker,
                other.semanticPartWithStabilityMarker
            )
            if (nameComparisonResult != 0) return nameComparisonResult

            // If they're identical, but only one has a non-semantic suffix, that's the larger one.
            // If both or neither have a non-semantic suffix, we move to the next step
            when {
                nonSemanticSuffix.isNullOrBlank() && !other.nonSemanticSuffix.isNullOrBlank() -> return -1
                !nonSemanticSuffix.isNullOrBlank() && other.nonSemanticSuffix.isNullOrBlank() -> return 1
            }

            // If both have a comparable non-semantic suffix, and they're different, that determines the result.
            // Blank/null suffixes aren't comparable, so if they're both null/blank, we move to the next step
            if (canBeUsedForComparison(nonSemanticSuffix) && canBeUsedForComparison(other.nonSemanticSuffix)) {
                val comparisonResult = VersionComparatorUtil.compare(versionName, other.versionName)
                if (comparisonResult != 0) return comparisonResult
            }

            // Fallback: neither has a comparable non-semantic suffix, we can consider it the same
            return 0
        }

        private fun canBeUsedForComparison(nonSemanticSuffix: String?): Boolean {
            if (nonSemanticSuffix.isNullOrBlank()) return false
            val normalizedSuffix = nonSemanticSuffix.trim().lowercase()
            val hasGitHashLength = normalizedSuffix.length in 7..10 || normalizedSuffix.length == 40
            return !(hasGitHashLength && normalizedSuffix.all { it.isDigit() || it in HEX_CHARS || !it.isLetter() })
        }

        companion object {

            private val HEX_CHARS = 'a'..'f'
        }
    }

    @Serializable
    @SerialName("timestamp-like")
    data class TimestampLike(
        override val versionName: String,
        override val isStable: Boolean,
        override val releasedAt: Instant?,
        val timestampPrefix: String,
        override val stabilityMarker: String?,
        override val nonSemanticSuffix: String?,
    ) : NormalizedVersion, DecoratedVersion {

        private val timestampPrefixWithStabilityMarker
            get() = timestampPrefix + (stabilityMarker ?: "")

        override fun compareTo(other: NormalizedVersion): Int =
            when (other) {
                is TimestampLike -> compareByNameAndThenByTimestamp(other)
                is Semantic -> -1
                is Garbage, is Missing -> 1
            }

        private fun compareByNameAndThenByTimestamp(other: TimestampLike): Int {
            val nameComparisonResult = VersionComparatorUtil.compare(
                timestampPrefixWithStabilityMarker,
                other.timestampPrefixWithStabilityMarker
            )

            return if (nameComparisonResult == 0) {
                compareByTimestamp(other)
            } else {
                nameComparisonResult
            }
        }
    }

    @Serializable
    @SerialName("garbage")
    data class Garbage(
        override val versionName: String,
        override val isStable: Boolean,
        override val releasedAt: Instant?,
    ) : NormalizedVersion {

        override fun compareTo(other: NormalizedVersion): Int =
            when (other) {
                is Missing -> 1
                is Garbage -> compareByNameAndThenByTimestamp(other)
                is Semantic, is TimestampLike -> -1
            }

        private fun compareByNameAndThenByTimestamp(other: Garbage): Int {
            val nameComparisonResult = VersionComparatorUtil.compare(versionName, other.versionName)
            return if (nameComparisonResult == 0) {
                compareByTimestamp(other)
            } else {
                nameComparisonResult
            }
        }
    }

    @Serializable
    @SerialName("missing")
    object Missing : NormalizedVersion {

        override val versionName: String = ""
        override val releasedAt: Instant? = null
        override val isStable: Boolean = false

        override fun compareTo(other: NormalizedVersion): Int =
            when (other) {
                is Missing -> 0
                else -> -1
            }
    }

    interface DecoratedVersion {

        val stabilityMarker: String?

        val nonSemanticSuffix: String?
    }
}


