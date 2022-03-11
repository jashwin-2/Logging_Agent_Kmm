package com.zoho.vtouch.logging_agent.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json



@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = JsonDataSerializer::class)
data class JsonData(val type: Int, val json: Any, val id: String) {
    companion object {
        const val TABLE_DATA: Int = 1
        const val LOG_MESSAGE: Int = 2
        const val GRAPH_DATA: Int = 3
        const val INITIAL_DATA: Int = 4
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }
}

@ExperimentalSerializationApi
object JsonDataSerializer : KSerializer<JsonData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Packet") {
        element("type", serialDescriptor<Int>())
        element("json", buildClassSerialDescriptor("Any"))
        element("id", serialDescriptor<String>())
    }

    @Suppress("UNCHECKED_CAST")
    private val dataTypeSerializers: Map<Int, KSerializer<Any>> =
        mapOf(
            2 to serializer<LogMessage>(),
            3 to serializer<List<GraphData>>(),
            4 to serializer<SessionDetails>(),
        ).mapValues { (_, v) -> v as KSerializer<Any> }

    private fun getJsonSerializer(dataType: Int): KSerializer<Any> =
        dataTypeSerializers[dataType]
            ?: throw SerializationException("Serializer for class $dataType is not registered in PacketSerializer")

    override fun serialize(encoder: Encoder, value: JsonData) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.type)
            encodeSerializableElement(descriptor, 1, getJsonSerializer(value.type), value.json)
            encodeStringElement(descriptor, 2, value.id)
        }
    }



    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        if (decodeSequentially()) {
            val type = decodeIntElement(descriptor, 0)
            val json = decodeSerializableElement(descriptor, 1, getJsonSerializer(type))
            val id = decodeStringElement(descriptor, 2)
            JsonData(type, json, id)
        } else {
            require(decodeElementIndex(descriptor) == 0) { "dataType field should precede payload field" }
            val type = decodeIntElement(descriptor, 0)
            val id = decodeStringElement(descriptor, 2)
            val json = when (val index = decodeElementIndex(descriptor)) {
                1 -> decodeSerializableElement(descriptor, 1, getJsonSerializer(type))
                CompositeDecoder.DECODE_DONE -> throw SerializationException("payload field is missing")
                else -> error("Unexpected index: $index")
            }
            JsonData(type, json, id)
        }
    }
}

