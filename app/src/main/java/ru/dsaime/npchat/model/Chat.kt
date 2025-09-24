package ru.dsaime.npchat.model

data class Chat(
    val id: String,
    val name: String,
    val chiefId: String,
)

data class Participant(
    val userId: String,
)
