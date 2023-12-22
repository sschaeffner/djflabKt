package xyz.schaeffner.djflab.web

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class SourceIdSerializer : KSerializer<SourceId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SourceId", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: SourceId) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): SourceId {
        val v = decoder.decodeInt()
        return SourceId.entries.first { it.value == v }
    }
}

@Serializable(with = SourceIdSerializer::class)
enum class SourceId(val value: Int) {
    SPOTIFY(1),
    AIRPLAY(2),
    AUX(3);

    companion object

    fun next(): SourceId =
        when (this) {
            SPOTIFY -> AIRPLAY
            AIRPLAY -> SPOTIFY // skip AUX, at the moment
            AUX -> SPOTIFY
        }
}