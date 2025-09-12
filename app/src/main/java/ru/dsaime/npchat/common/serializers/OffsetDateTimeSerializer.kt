package ru.dsaime.npchat.common.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class OffsetDateTimeNullableSerializer : KSerializer<OffsetDateTime?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.LONG)

    override fun serialize(
        encoder: Encoder,
        value: OffsetDateTime?,
    ) {
        encoder.encodeLong(value?.toEpochSecond() ?: -1)
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime? {
        val epochSecond = decoder.decodeLong()
        if (epochSecond > 9999999999) {
            return OffsetDateTime.MAX
        } else if (epochSecond < -9999999999) {
            return OffsetDateTime.MIN
        }
        return OffsetDateTime.of(
            LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC),
            ZoneOffset.UTC,
        )
    }
}

class OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.LONG)

    override fun serialize(
        encoder: Encoder,
        value: OffsetDateTime,
    ) {
        encoder.encodeLong(value.toEpochSecond())
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val epochSecond = decoder.decodeLong().coerceAtLeast(0)
        if (epochSecond > 9999999999) {
            return OffsetDateTime.MAX
        } else if (epochSecond < -9999999999) {
            return OffsetDateTime.MIN
        }
        return epochSecond.sToOffsetDateTimeUTC()
    }
}

class OffsetDateTimeStringSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: OffsetDateTime,
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val str = decoder.decodeString()

        return OffsetDateTime.parse(str)
    }
}

fun Long?.sToOffsetDateTimeUTC(): OffsetDateTime {
    if (this == null || this < 0) {
        return OffsetDateTime.MIN
    }
    if (this > 4070912400) {
        return OffsetDateTime.MAX
    }
    return OffsetDateTime.ofInstant(Instant.ofEpochSecond(this), ZoneOffset.UTC)
}
