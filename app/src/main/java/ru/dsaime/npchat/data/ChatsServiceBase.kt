package ru.dsaime.npchat.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import ru.dsaime.npchat.model.Chat

class ChatsServiceBase(
    private val api: NPChatApi,
) : ChatsService {
    override suspend fun myChats(keyset: String): Result<MyChatsResult, String> =
        api
            .chats(keyset)
            .mapCatching {
                MyChatsResult(
                    chats = it.chats.map(ApiModel.Chat::toModel),
                    nextKeyset = it.nextKeyset.orEmpty(),
                ).run(::Ok)
            }.getOrElse { Err(it.toUserMessage()) }

    override suspend fun create(name: String): Chat {
        TODO("Not yet implemented")
    }

    override suspend fun leave(id: String) {
        TODO("Not yet implemented")
    }
}

fun ApiModel.Chat.toModel(): Chat =
    Chat(
        id = id,
        name = name,
        chiefId = chiefId,
    )
