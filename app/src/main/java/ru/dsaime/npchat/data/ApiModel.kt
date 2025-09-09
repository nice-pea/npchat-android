package ru.dsaime.npchat.data

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

object ApiModel {
    data class RegistrationBody(
        @SerializedName("login") val login: String,
        @SerializedName("password") val password: String,
        @SerializedName("name") val name: String,
        @SerializedName("nick") val nick: String,
    )

    data class LoginBody(
        @SerializedName("login") val login: String,
        @SerializedName("password") val password: String,
    )

    data class AuthResp(
        @SerializedName("User") val user: User,
        @SerializedName("Session") val session: Session,
    )

    data class User(
        @SerializedName("Id") val id: String,
        @SerializedName("Name") val name: String,
        @SerializedName("Nick") val nick: String,
    )

    data class Session(
        @SerializedName("Id") val id: String,
        @SerializedName("UserId") val userId: String,
        @SerializedName("Name") val name: String,
        @SerializedName("Status") val status: String,
        @SerializedName("AccessToken") val accessToken: Token,
        @SerializedName("RefreshToken") val refreshToken: Token
    )

    data class Token(
        @SerializedName("Token") val token: String,
        @SerializedName("Expiry") val expiry: OffsetDateTime
    )
}