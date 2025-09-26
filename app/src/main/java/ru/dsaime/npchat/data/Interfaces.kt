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

    fun currentHostFlow(): StateFlow<Host?>

    suspend fun changeHost(host: Host)

    suspend fun deleteHostByUrl(url: String)

    fun hostsStateFlow(): StateFlow<List<Host>>

    suspend fun status(baseUrl: String): Host.Status

    fun statusFlow(baseUrl: String): StateFlow<Host.Status>

    suspend fun add(host: Host)
}

// Описывает интерфейс работы с сессией
interface SessionsService {
    suspend fun currentSession(): Session?

    fun currentSessionFlow(): StateFlow<Session?>

    suspend fun changeSession(session: Session)

    suspend fun isActual(session: Session): Boolean

    suspend fun refresh(session: Session): Boolean

    suspend fun me(): Result<User, String>
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

    fun onChatUpdated(): Flow<Result<Event.ChatUpdated, String>>

    fun onChatCreated(): Flow<Result<Event.ChatCreated, String>>
}
