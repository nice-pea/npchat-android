package ru.dsaime.npchat.data.api.model

import ru.dsaime.npchat.model.Model

object ApiModel {
    data class Authn(
        val user: Model.User,
        val session: Model.Session
    )

    data class Login(
        val user: Model.User,
        val session: Model.Session
    )

    data class Chats(
        val chats: List<Model.Chat>,
    )
}