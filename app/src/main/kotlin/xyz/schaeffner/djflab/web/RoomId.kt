package xyz.schaeffner.djflab.web

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class RoomIdSerializer : KSerializer<RoomId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RoomId", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: RoomId) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): RoomId {
        val v = decoder.decodeInt()
        return RoomId.entries.first { it.value == v }
    }
}

@Serializable(with = RoomIdSerializer::class)
enum class RoomId(val value: Int) {
    SOCIAL_ZONE(1),
    CREATIVE_ZONE(2),
    LASER_ZONE(3),
    WORK_ZONE(4)
}
