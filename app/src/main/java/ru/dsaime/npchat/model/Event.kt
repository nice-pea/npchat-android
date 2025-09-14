package ru.dsaime.npchat.model

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime


data class Event(
    @SerializedName("Type") val type: String, // Тип события
    @SerializedName("CreatedIn") val createdIn: OffsetDateTime, // Время создания
    @SerializedName("Data") val data: Map<String, Any>, // Полезная нагрузка
)
