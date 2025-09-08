package ru.dsaime.npchat.model

import java.time.OffsetDateTime

data class Session(
    val id: Int,
    val name: String,
    val refreshToken: String,
    val status: String,
    val refreshTokenExpiresAt: OffsetDateTime,
    val accessToken: String,
    val accessTokenExpiresAt: OffsetDateTime,
)