package ru.dsaime.npchat.data.api

import retrofit2.http.GET
import retrofit2.http.Path


interface NpcClientApi {
    @GET("{url}/health")
    suspend fun health(
        @Path("url", encoded = true) url: String
    ): Result<Unit>
}