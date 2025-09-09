package ru.dsaime.npchat.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

private const val overrideHostHeader = "X-Override-Host"

interface NPChatApiDyn {
    @GET("/ping")
    suspend fun ping(
        @Header(overrideHostHeader) host: String,
    ): Result<Unit>

    @POST("/auth/password/registration")
    suspend fun registration(
        @Header(overrideHostHeader) host: String,
        @Body body: ApiModel.RegistrationBody,
    ): Result<ApiModel.AuthResp>

    @POST("/auth/password/login")
    suspend fun login(
        @Header(overrideHostHeader) host: String,
        @Body body: ApiModel.LoginBody,
    ): Result<ApiModel.AuthResp>
}

interface NPChatApi {
    @GET("/me")
    suspend fun me(): Result<ApiModel.User>
}