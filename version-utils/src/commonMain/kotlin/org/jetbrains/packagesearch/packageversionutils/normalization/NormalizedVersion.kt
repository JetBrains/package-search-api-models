package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.packagesearch.packageversionutils.PackageVersionUtils

@Serializable
public sealed interface NormalizedVersion : Comparable<NormalizedVersion?> {

    public companion object {

        public fun fromString(versionName: String, releasedAt: Instant? = null): NormalizedVersion =
            versionName.normalize(
                isStable = PackageVersionUtils.evaluateStability(versionName),
                releasedAt = releasedAt,
            ) {
                Garbage(
                    versionName = versionName,
                    isStable = PackageVersionUtils.evaluateStability(versionName),
                    releasedAt = releasedAt,
                )
            }

    }

    public val versionName: String
    public val isStable: Boolean
    public val releasedAt: Instant?

    @Serializable
    @SerialName("semantic")
    public data class Semantic(
        public override val versionName: String,
        public override val isStable: Boolean,
        public override val releasedAt: Instant? = null,
        public val semanticPart: String,
        public override val stabilityMarker: String? = null,
        public override val nonSemanticSuffix: String? = null,
    ) : NormalizedVersion, DecoratedVersion {

        public val semanticPartWithStabilityMarker: String
            get() = semanticPart + (stabilityMarker ?: "")

        public override fun compareTo(other: NormalizedVersion?): Int =
            when (other) {
                is Semantic -> compareByNameAndThenByTimestamp(other)
                is TimestampLike, is Garbage, null -> 1
            }

        private fun compareByNameAndThenByTimestamp(other: Semantic): Int {
            // First, compare semantic parts and stability markers only
            val nameComparisonResult = VersionComparatorUtil.compare(
                semanticPartWithStabilityMarker,
                other.semanticPartWithStabilityMarker,
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

        public companion object {

            private val HEX_CHARS: CharRange = 'a'..'f'
        }
    }

    @Serializable
    @SerialName("timestamp-like")
    public data class TimestampLike(
        public override val versionName: String,
        public override val isStable: Boolean,
        public override val releasedAt: Instant? = null,
        public val timestampPrefix: String,
        public override val stabilityMarker: String? = null,
        public override val nonSemanticSuffix: String? = null,
    ) : NormalizedVersion, DecoratedVersion {

        private val timestampPrefixWithStabilityMarker
            get() = timestampPrefix + (stabilityMarker ?: "")

        override fun compareTo(other: NormalizedVersion?): Int =
            when (other) {
                is TimestampLike -> compareByNameAndThenByTimestamp(other)
                is Semantic -> -1
                is Garbage, null -> 1
            }

        private fun compareByNameAndThenByTimestamp(other: TimestampLike): Int {
            val nameComparisonResult = VersionComparatorUtil.compare(
                timestampPrefixWithStabilityMarker,
                other.timestampPrefixWithStabilityMarker,
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
    public data class Garbage(
        public override val versionName: String,
        public override val isStable: Boolean,
        public override val releasedAt: Instant? = null,
    ) : NormalizedVersion {

        override fun compareTo(other: NormalizedVersion?): Int =
            when (other) {
                is Garbage -> compareByNameAndThenByTimestamp(other)
                is Semantic, is TimestampLike, null -> -1
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

    public interface DecoratedVersion {
        public val stabilityMarker: String?
        public val nonSemanticSuffix: String?
    }
}
