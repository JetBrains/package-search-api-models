package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object CompactNormalizedVersionSerializer : KSerializer<NormalizedVersion> {
    override val descriptor: SerialDescriptor
        get() = CompactVersion.serializer().descriptor

    override fun deserialize(decoder: Decoder): NormalizedVersion {
        val compactVersion = decoder.decodeSerializableValue(CompactVersion.serializer())
        return NormalizedVersion.fromString(compactVersion.version, compactVersion.releasedAt)
            ?: throw SerializationException("Failed to deserialize version into a NormalizedVersion from ${compactVersion.version}")
    }

    override fun serialize(encoder: Encoder, value: NormalizedVersion) {
        encoder.encodeSerializableValue(
            serializer = CompactVersion.serializer(),
            value = CompactVersion(value.versionName, value.releasedAt)
        )
    }
}

@Serializable
public data class CompactVersion(
    val version: String,
    val releasedAt: Instant? = null,
)
