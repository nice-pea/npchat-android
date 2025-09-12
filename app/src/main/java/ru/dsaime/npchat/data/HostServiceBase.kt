package ru.dsaime.npchat.data

import kotlinx.coroutines.Dispatchers
import ru.dsaime.npchat.data.room.AppDatabase
import ru.dsaime.npchat.data.room.Host

class HostServiceBase(
    private val api: NPChatApi,
    private val db: AppDatabase,
) : HostService {
    override suspend fun currentHost() =
        with(Dispatchers.IO) {
            db
                .hostDao()
                .getAll()
                .maxByOrNull { it.lastUsed }
                ?.baseUrl
        }

    override suspend fun changeHost(host: String) {
        db.hostDao().insertAll(Host(baseUrl = host))
    }

    override suspend fun known(): List<String> =
        db
            .hostDao()
            .getAll()
            .sortedByDescending { it.lastUsed }
            .map { it.baseUrl }

    override suspend fun ping(host: String) = api.ping(host).isSuccess
}
