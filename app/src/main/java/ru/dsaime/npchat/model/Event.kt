package ru.dsaime.npchat.model

import java.time.OffsetDateTime


sealed interface Event {
    data class Head(
        val type: String, // Тип события
        val createdIn: OffsetDateTime, // Время создания
//        val data: Map<String, Any>, // Полезная нагрузка
    )
}