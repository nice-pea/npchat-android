package ru.dsaime.npchat.data

import ru.dsaime.npchat.data.ApiModel
import ru.dsaime.npchat.network.authzHeaderValue

class NPChatRepository(
    private val api: NPChatApi,
    private val localPrefs: NPChatLocalPrefs,
) {
    suspend fun authn(token: String, server: String = localPrefs.baseUrl): Result<ApiModel.Authn> {
        return api.authn(
            server = server,
            token = authzHeaderValue(token),
        )
    }

    suspend fun login(key: String, server: String = localPrefs.baseUrl): Result<ApiModel.Login> {
        return api.login(
            server = server,
            key = key,
        )
    }
}