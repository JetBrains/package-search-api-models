package org.jetbrains.packagesearch.maven

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlBufferedReader
import nl.adaptivity.xmlutil.XmlWriter
import nl.adaptivity.xmlutil.consecutiveTextContent
import nl.adaptivity.xmlutil.serialization.XML

object MavenPomPropertiesXmlSerializer : KSerializer<Properties> {
    private val fallbackSerializer = MapSerializer(String.serializer(), String.serializer())

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("maven.properties") {
        element("properties", fallbackSerializer.descriptor)
    }

    private val Decoder.xmlReaderOrNull
        get() = (this as? XML.XmlInput)?.input as? XmlBufferedReader

    private val Encoder.xmlWriterOrNull
        get() = (this as? XML.XmlOutput)?.target

    private fun XmlWriter.encodeProperties(properties: Map<String, String>) {
        startTag(POM_XML_NAMESPACE, "properties", "")
        properties.forEach { (k, v) ->
            startTag(POM_XML_NAMESPACE, k, "")
            text(v)
            endTag(POM_XML_NAMESPACE, k, "")
        }
        endTag(POM_XML_NAMESPACE, "properties", "")
    }

    private fun XmlBufferedReader.decodeProperties(): Map<String, String> = buildMap {
        if (localName != "properties") throw SerializationException("Expected properties tag")
        nextTag()
        while (hasNext()) {
            if (localName == "properties" && eventType == EventType.END_ELEMENT) break
            if (eventType == EventType.START_ELEMENT) {
                if (localName == "property") {
                    var name: String? = null
                    var value: String? = null
                    while (hasNext() && !(eventType == EventType.END_ELEMENT && localName == "property")) {
                        if (eventType == EventType.START_ELEMENT) {
                            when (localName) {
                                "name" -> name = consecutiveTextContent()
                                "value" -> value = consecutiveTextContent()
                            }
                        }
                        nextTag()
                    }
                    name ?: throw SerializationException("Property name is not specified")
                    value ?: throw SerializationException("Property value for name '$name' is not specified")
                    set(name, value)
                } else {
                    set(localName, consecutiveTextContent())
                }
            }
            nextTag()
        }
    }

    override fun deserialize(decoder: Decoder) =
        Properties(decoder.xmlReaderOrNull?.decodeProperties() ?: fallbackSerializer.deserialize(decoder))

    override fun serialize(encoder: Encoder, value: Properties) =
        encoder.xmlWriterOrNull?.encodeProperties(value.properties)
            ?: fallbackSerializer.serialize(encoder, value.properties)

}