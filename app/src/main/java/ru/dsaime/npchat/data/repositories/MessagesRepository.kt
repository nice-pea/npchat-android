package ru.dsaime.npchat.data.repositories

import ru.dsaime.npchat.data.api.MessagesApi
import ru.dsaime.npchat.model.Model

enum class Boundary {
    Around,
    Before,
    After,
}

class MessagesRepository(
    private val api: MessagesApi
) {
    suspend fun messages(
        chatID: Int,
        id: Int?,
        limit: Int,
        boundary: Boundary? = null,
        authorID: Int? = null,
        replyToID: Int? = null,
    ): Result<List<Model.Message>> {
        val id = id ?: 0
        return api.messages(
            ids = listOfNotNull(id.takeIf { boundary != null }),
            chatIDs = listOf(chatID),
            authorIDs = listOfNotNull(authorID),
            replyToIDs = listOfNotNull(replyToID),
            aroundID = id.takeIf { boundary == Boundary.Around },
            beforeID = id.takeIf { boundary == Boundary.Before },
            afterID = id.takeIf { boundary == Boundary.After },
            limit = limit
        )
    }
}