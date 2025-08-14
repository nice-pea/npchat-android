package ru.dsaime.npchat.data.api.model

import com.google.gson.annotations.SerializedName
import java.util.Date

object ApiModel {
    data class RegistrationBody(
        @SerializedName("Login")  val login: String,
        @SerializedName("Password")  val password: String,
        @SerializedName("Name")  val name: String,
        @SerializedName("Nick")  val nick: String,
    )

    data class LoginBody(
        @SerializedName("Login")  val login: String,
        @SerializedName("Password")  val password: String,
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
        @SerializedName("Expiry") val expiry: Date
    )

//    data class Authn(
//        val user: Model.User,
//        val session: Model.Session
//    )
//
//    data class Login(
//        val user: Model.User,
//        val session: Model.Session
//    )
//
//    data class Chats(
//        val chats: List<Model.Chat>,
//    )
}