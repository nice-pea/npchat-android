package ru.dsaime.npchat.model

import java.time.OffsetDateTime


data class Event(
    val type: String, // Тип события
    val createdIn: OffsetDateTime, // Время создания
//        Recipients []uuid.UUID    // Получатели (id пользователей)
    val data: Map<String, Any>, // Полезная нагрузка
)