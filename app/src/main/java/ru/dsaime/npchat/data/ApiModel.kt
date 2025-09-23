package ru.dsaime.npchat.data

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

object ApiModel {
    data class Participant(
        @SerializedName("UserID") val userId: String,
    ) {
        fun toModel() =
            ru.dsaime.npchat.model.Participant(
                userId = userId,
            )
    }

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
        @SerializedName("user") val user: User,
        @SerializedName("session") val session: Session,
    )

    data class User(
        @SerializedName("ID") val id: String,
        @SerializedName("Name") val name: String,
        @SerializedName("Nick") val nick: String,
    ) {
        fun toModel() =
            ru.dsaime.npchat.model.User(
                id = id,
                name = name,
                nick = nick,
            )
    }

    data class Session(
        @SerializedName("ID") val id: String,
        @SerializedName("UserID") val userId: String,
        @SerializedName("Name") val name: String,
        @SerializedName("Status") val status: String,
        @SerializedName("AccessToken") val accessToken: Token,
        @SerializedName("RefreshToken") val refreshToken: Token,
    )

    data class Token(
        @SerializedName("Token") val token: String,
        @SerializedName("Expiry") val expiry: OffsetDateTime,
    )

    data class Chat(
        @SerializedName("ID") val id: String,
        @SerializedName("Name") val name: String,
        @SerializedName("ChiefID") val chiefId: String,
    ) {
        fun toModel() =
            ru.dsaime.npchat.model.Chat(
                id = id,
                name = name,
                chiefId = chiefId,
            )
    }

    data class ChatsResp(
        @SerializedName("next_page_token") val nextPageToken: String?,
        @SerializedName("Chats") val chats: List<Chat>,
    )

    data class CreateChatBody(
        @SerializedName("Name") val name: String,
    )

    data class CreateChatResp(
        @SerializedName("Chat") val chat: Chat,
    )
}
