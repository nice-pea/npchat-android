package ru.dsaime.npchat.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.dsaime.npchat.data.api.model.ApiModel

interface ChatsApi {
    @GET("/chats")
    suspend fun chats(
        @Query("unread_counter") unreadForUserID: Int? = null,
        @Query("ids") ids: List<Int> = emptyList(),
    ): Result<ApiModel.Chats>
}