package org.jetbrains.packagesearch.packageversionutils.normalization

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.packagesearch.packageversionutils.WeakValueMap
import org.jetbrains.packagesearch.packageversionutils.getOrPut

object NormalizedVersionWeakCache : KSerializer<NormalizedVersion> {

    private val weakCache = WeakValueMap<Key, NormalizedVersion>()

    data class Key(val versionName: String, val releasedAt: Instant?)

    override val descriptor = NormalizedVersion.serializer().descriptor

    override fun deserialize(decoder: Decoder): NormalizedVersion {
        val deserialized = NormalizedVersion.serializer().deserialize(decoder)
        return if (deserialized is NormalizedVersion.Missing) deserialized
        else weakCache.getOrPut(deserialized.key) { deserialized }
    }

    override fun serialize(encoder: Encoder, value: NormalizedVersion) {
        NormalizedVersion.serializer().serialize(encoder, value)
    }

    fun getOrPut(versionName: String, releasedAt: Instant?, compute: () -> NormalizedVersion): NormalizedVersion {
        return  weakCache.getOrPut(Key(versionName, releasedAt)) { compute() }
    }
}

