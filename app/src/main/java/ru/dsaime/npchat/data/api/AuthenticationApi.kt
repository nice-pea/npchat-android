package ru.dsaime.npchat.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import ru.dsaime.npchat.data.api.model.ApiModel
import ru.dsaime.npchat.network.AuthorizationHeader

interface AuthenticationApi {
    @GET("{server}/authn")
    suspend fun authn(
        @Path("server", encoded = true) server: String,
        @Header(AuthorizationHeader) token: String,
    ): Result<ApiModel.Authn>

    @GET("{server}/authn/login")
    suspend fun login(
        @Path("server", encoded = true) server: String,
        @Query("key") key: String,
    ): Result<ApiModel.Login>
}
