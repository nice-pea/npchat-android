package ru.dsaime.npchat.data

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import ru.dsaime.npchat.model.Chat
import ru.dsaime.npchat.model.Event
import ru.dsaime.npchat.model.Session
import ru.dsaime.npchat.model.User

// Описывает интерфейс аутентификации
interface BasicAuthService {
    // Результат аутентификации
    data class AuthResult(
        val user: User,
        val session: Session,
    )

    // Вход по логину и паролю
    suspend fun login(
        login: String,
        pass: String,
        host: String,
    ): Result<AuthResult, String>

    // Регистрация по логину и паролю
    suspend fun registration(
        login: String,
        nick: String,
        name: String,
        pass: String,
        host: String,
    ): Result<AuthResult, String>
}

// Описывает интерфейс работы с хостами
interface HostService {
    suspend fun currentHost(): String?

    suspend fun changeHost(host: String)

    suspend fun known(): List<String>

    // Возвращает сервер по специальному алгоритму
    suspend fun preferredHost(): String?

    suspend fun ping(host: String): Boolean
}

// Описывает интерфейс работы с сессией
interface SessionsService {
    suspend fun currentSession(): Session?

    suspend fun changeSession(session: Session)

    suspend fun isActual(session: Session): Boolean

    suspend fun refresh(session: Session): Boolean
}

// Описывает интерфейс доступа к потоку событий
interface EventsFlowProvider {
    suspend fun eventsFlow(session: Session): Flow<Event>
}

interface ChatsService {
    suspend fun myChats(pageToken: String): Result<MyChatsResult, String>

    suspend fun create(name: String): Chat

    suspend fun leave(id: String)
}

class MyChatsResult(
    val chats: List<Chat>,
    val nextPageToken: String,
)
