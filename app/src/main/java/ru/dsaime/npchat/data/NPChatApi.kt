package ru.dsaime.npchat.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

private const val OVERRIDE_HOST_HEADER = "X-Override-Host"

interface NPChatApi {
    @GET("/ping")
    suspend fun ping(
        @Header(OVERRIDE_HOST_HEADER) host: String,
    ): Result<Unit>

    @POST("/auth/password/registration")
    suspend fun registration(
        @Header(OVERRIDE_HOST_HEADER) host: String,
        @Body body: ApiModel.RegistrationBody,
    ): Result<ApiModel.AuthResp>

    @POST("/auth/password/login")
    suspend fun login(
        @Header(OVERRIDE_HOST_HEADER) host: String,
        @Body body: ApiModel.LoginBody,
    ): Result<ApiModel.AuthResp>

    @POST("/sessions/{id}/revoke")
    suspend fun revoke(
        @Path("id") id: String,
    ): Result<ApiModel.AuthResp>

    @GET("/me")
    suspend fun me(): Result<ApiModel.MeResp>

    @GET("/chats")
    suspend fun chats(
        @Query("page_token") pageToken: String,
    ): Result<ApiModel.ChatsResp>

    @POST("/chats")
    suspend fun createChat(
        @Body body: ApiModel.CreateChatBody,
    ): Result<ApiModel.CreateChatResp>

    @GET("/chat/{chatId}/leave")
    suspend fun leaveChat(
        @Path("chatId") chatId: String,
    ): Result<Unit>
}
