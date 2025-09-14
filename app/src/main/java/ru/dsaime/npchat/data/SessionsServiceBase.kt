package ru.dsaime.npchat.data

import ru.dsaime.npchat.data.room.AppDatabase
import ru.dsaime.npchat.model.Session

class SessionsServiceBase(
    private val api: NPChatApi,
    private val hostService: HostService,
    private val db: AppDatabase,
) : SessionsService {
    private var currentSession: Session? = null

    override fun currentSession() = currentSession

    override fun changeSession(session: Session) {
        currentSession = session
    }

    override suspend fun isActual(session: Session): Boolean {
        if (currentSession == null || hostService.currentHost() == null) {
            return false
        }

        return api.chats().isSuccess
    }

    override suspend fun refresh(session: Session): Boolean {
        TODO("Not yet implemented")
    }
}
