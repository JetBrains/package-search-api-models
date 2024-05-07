package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object CompactNormalizedVersionSerializer : KSerializer<NormalizedVersion> {

    @Serializable
    private data class CompactNormalizedVersion(
        val versionName: String,
        val releasedAt: Instant?,
    )

    override val descriptor: SerialDescriptor
        get() = CompactNormalizedVersion.serializer().descriptor

    override fun deserialize(decoder: Decoder): NormalizedVersion {
        val compactVersion = CompactNormalizedVersion.serializer().deserialize(decoder)
        return NormalizedVersion.from(compactVersion.versionName, compactVersion.releasedAt)
    }

    override fun serialize(encoder: Encoder, value: NormalizedVersion) {
        val compactVersion = CompactNormalizedVersion(value.versionName, value.releasedAt)
        CompactNormalizedVersion.serializer().serialize(encoder, compactVersion)
    }

}