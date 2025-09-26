package ru.dsaime.npchat.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import ru.dsaime.npchat.model.Chat

class ChatsServiceBase(
    private val api: NPChatApi,
) : ChatsService {
    override suspend fun myChats(pageToken: String): Result<MyChatsResult, String> =
        api
            .chats(pageToken)
            .mapCatching {
                MyChatsResult(
                    chats = it.chats?.map(ApiModel.Chat::toModel).orEmpty(),
                    nextPageToken = it.nextPageToken.orEmpty(),
                ).run(::Ok)
            }.getOrElse { Err(it.toUserMessage()) }

    override suspend fun create(name: String): Result<Chat, String> =
        api
            .createChat(
                ApiModel.CreateChatBody(
                    name = name,
                ),
            ).mapCatching { Ok(it.chat.toModel()) }
            .getOrElse { Err(it.toUserMessage()) }

    override suspend fun leave(id: String) =
        api
            .leaveChat(chatId = id)
            .mapCatching { Ok(Unit) }
            .getOrElse { Err(it.toUserMessage()) }
}
