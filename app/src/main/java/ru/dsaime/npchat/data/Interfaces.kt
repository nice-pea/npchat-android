package ru.dsaime.npchat.data

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.dsaime.npchat.model.Chat
import ru.dsaime.npchat.model.Event
import ru.dsaime.npchat.model.Host
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
    suspend fun currentBaseUrl(): String?

    fun currentBaseUrlFlow(): StateFlow<String?>

    suspend fun changeBaseUrl(baseUrl: String)

    suspend fun deleteBaseUrl(baseUrl: String)

    suspend fun savedBaseUrls(): List<String>

    suspend fun ping(baseUrl: String): Boolean

    fun statusFlow(baseUrl: String): StateFlow<Host.Status>
}

// Описывает интерфейс работы с сессией
interface SessionsService {
    suspend fun currentSession(): Session?

    fun currentSessionFlow(): StateFlow<Session?>

    suspend fun changeSession(session: Session)

    suspend fun isActual(session: Session): Boolean

    suspend fun refresh(session: Session): Boolean
}

// Описывает интерфейс доступа к потоку событий
interface EventsFlowProvider {
    suspend fun eventsFlow(session: Session): Flow<Result<Event, String>>
}

interface ChatsService {
    suspend fun myChats(pageToken: String): Result<MyChatsResult, String>

    suspend fun create(name: String): Result<Chat, String>

    suspend fun leave(id: String): Result<Unit, String>
}

class MyChatsResult(
    val chats: List<Chat>,
    val nextPageToken: String,
)

interface EventsService {
    fun onParticipantAdded(): Flow<Result<Event.ParticipantAdded, String>>

    fun onParticipantRemoved(): Flow<Result<Event.ParticipantRemoved, String>>

    fun onChatNameUpdated(): Flow<Result<Event.ChatNameUpdated, String>>

    fun onChatCreated(): Flow<Result<Event.ChatCreated, String>>
}
