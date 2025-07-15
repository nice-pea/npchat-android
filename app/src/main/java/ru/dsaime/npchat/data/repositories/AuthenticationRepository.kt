package ru.dsaime.npchat.data.repositories

import ru.dsaime.npchat.data.api.AuthenticationApi
import ru.dsaime.npchat.data.api.model.ApiModel
import ru.dsaime.npchat.data.store.NpcClientStore
import ru.dsaime.npchat.network.authzHeaderValue

class AuthenticationRepository(
    private val api: AuthenticationApi,
    private val npcStore: NpcClientStore,
) {
    suspend fun authn(token: String, server: String = npcStore.baseUrl): Result<ApiModel.Authn> {
        return api.authn(
            server = server,
            token = authzHeaderValue(token),
        )
    }

    suspend fun login(key: String, server: String = npcStore.baseUrl): Result<ApiModel.Login> {
        return api.login(
            server = server,
            key = key,
        )
    }
}