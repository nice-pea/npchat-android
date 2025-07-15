package ru.dsaime.npchat.data.repositories

import ru.dsaime.npchat.data.api.NpcClientApi
import ru.dsaime.npchat.data.store.NpcClientStore
import ru.dsaime.npchat.network.npcBaseUrl

class NpcClient(
    private val api: NpcClientApi,
    private val npcStore: NpcClientStore,
) {
    suspend fun healthCheck(url: String = ""): Result<Unit> {
        return api.health(url = npcBaseUrl(npcStore, url))
    }
}