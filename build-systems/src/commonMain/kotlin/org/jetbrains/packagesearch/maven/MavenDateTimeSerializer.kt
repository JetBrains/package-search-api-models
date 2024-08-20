package org.jetbrains.packagesearch.maven

import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object MavenDateTimeSerializer : KSerializer<Instant> {

    public val format: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Companion.Format {
        year()
        monthNumber()
        dayOfMonth()
        hour()
        minute()
        second()
    }

    override val descriptor: SerialDescriptor
        get() = String.Companion.serializer().descriptor

    // format yyyyMMddHHmmss
    override fun deserialize(decoder: Decoder): Instant =
        Instant.Companion.parse(decoder.decodeString(), format)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.format(format))
    }

}