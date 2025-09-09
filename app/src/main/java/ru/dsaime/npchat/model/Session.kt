package ru.dsaime.npchat.model

import java.time.OffsetDateTime

data class Session(
    val id: String,
    val name: String,
    val status: String,
    val refreshToken: String,
    val refreshTokenExpiresAt: OffsetDateTime,
    val accessToken: String,
    val accessTokenExpiresAt: OffsetDateTime,
)