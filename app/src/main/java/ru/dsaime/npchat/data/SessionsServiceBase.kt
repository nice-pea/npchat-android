package ru.dsaime.npchat.data

import kotlinx.coroutines.Dispatchers
import ru.dsaime.npchat.data.room.AppDatabase
import ru.dsaime.npchat.model.Session

class SessionsServiceBase(
    private val api: NPChatApi,
    private val hostService: HostService,
    private val db: AppDatabase,
) : SessionsService {
    override suspend fun currentSession() =
        with(Dispatchers.IO) {
            db
                .sessionDao()
                .last()
                ?.toModel()
        }

    override suspend fun changeSession(session: Session) {
        db.sessionDao().upsert(
            ru.dsaime.npchat.data.room
                .Session(session),
        )
    }

    override suspend fun isActual(session: Session): Boolean {
        if (db.sessionDao().last() == null || hostService.currentHost() == null) {
            return false
        }

        return api.chats("").isSuccess
    }

    override suspend fun refresh(session: Session): Boolean {
        TODO("Not yet implemented")
    }
}
