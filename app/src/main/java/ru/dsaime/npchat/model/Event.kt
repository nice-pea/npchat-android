package ru.dsaime.npchat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import ru.dsaime.npchat.common.serializers.OffsetDateTimeStringSerializer
import java.time.OffsetDateTime


@Serializable
data class Event(
    @SerialName("Type") val type: String, // Тип события
    @Serializable(with = OffsetDateTimeStringSerializer::class)
    @SerialName("CreatedIn") val createdIn: OffsetDateTime, // Время создания
//        Recipients []uuid.UUID    // Получатели (id пользователей)
    @SerialName("Data") val data: JsonObject, // Полезная нагрузка
)