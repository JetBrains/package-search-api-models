package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.packagesearch.packageversionutils.WeakValueMap
import org.jetbrains.packagesearch.packageversionutils.getOrPut

public object NormalizedVersionWeakCache : KSerializer<NormalizedVersion> {

    private val weakCache = WeakValueMap<Key, NormalizedVersion>()

    public data class Key(val versionName: String, val releasedAt: Instant?)

    public override val descriptor: SerialDescriptor = NormalizedVersion.serializer().descriptor

    public override fun deserialize(decoder: Decoder): NormalizedVersion {
        val deserialized = NormalizedVersion.serializer().deserialize(decoder)
        return if (deserialized is NormalizedVersion.Missing) {
            deserialized
        } else {
            weakCache.getOrPut(deserialized.key) { deserialized }
        }
    }

    public override fun serialize(encoder: Encoder, value: NormalizedVersion) {
        NormalizedVersion.serializer().serialize(encoder, value)
    }

    public fun getOrPut(versionName: String, releasedAt: Instant?, compute: () -> NormalizedVersion): NormalizedVersion {
        return weakCache.getOrPut(Key(versionName, releasedAt)) { compute() }
    }
}
