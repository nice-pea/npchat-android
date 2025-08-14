package ru.dsaime.npchat.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.dsaime.npchat.data.api.model.ApiModel

interface NPChatApi {
    @GET("{server}/ping")
    suspend fun ping(
        @Path("server", encoded = true) server: String,
    ): Result<Unit>

    @POST("{server}/auth/password/registration")
    suspend fun auth(
        @Path("server", encoded = true) server: String,
        @Body body: ApiModel.RegistrationBody,
    ): Result<ApiModel.Authn>

    @POST("{server}/auth/password/login")
    suspend fun login(
        @Path("server", encoded = true) server: String,
        @Body body: ApiModel.LoginBody,
    ): Result<ApiModel.Login>
}