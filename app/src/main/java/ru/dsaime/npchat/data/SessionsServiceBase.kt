package ru.dsaime.npchat.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.dsaime.npchat.data.room.AppDatabase
import ru.dsaime.npchat.model.Session

class SessionsServiceBase(
    private val api: NPChatApi,
    private val hostService: HostService,
    private val db: AppDatabase,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : SessionsService {
    override suspend fun currentSession() =
        with(Dispatchers.IO) {
            db
                .sessionDao()
                .last()
                ?.toModel()
        }

    override fun currentSessionFlow(): StateFlow<Session?> {
        val initial = runBlocking { currentSession() }
        val flow = MutableStateFlow(initial)
        coroutineScope.launch {
            db
                .sessionDao()
                .lastFlow()
                .map { it?.toModel() }
                .collect { flow.value = it }
        }

        return flow
    }

    override suspend fun changeSession(session: Session) {
        db.sessionDao().upsert(
            ru.dsaime.npchat.data.room
                .Session(session),
        )
    }

    override suspend fun isActual(session: Session): Boolean {
        if (db.sessionDao().last() == null || hostService.currentBaseUrl() == null) {
            return false
        }

        return api.chats("").isSuccess
    }

    override suspend fun refresh(session: Session): Boolean {
        TODO("Not yet implemented")
    }
}
