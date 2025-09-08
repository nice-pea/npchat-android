package ru.dsaime.npchat.data

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import ru.dsaime.npchat.model.Event
import ru.dsaime.npchat.model.Session
import ru.dsaime.npchat.model.User

// Описывает интерфейс аутентификации
interface AuthService {
    // Результат аутентификации
    data class AuthResult(
        val user: User,
        val session: Session,
    )

    // Вход по логину и паролю
    fun login(
        login: String,
        pass: String
    ): Result<AuthResult, String>

    // Регистрация по логину и паролю
    fun registration(
        login: String,
        nick: String,
        name: String,
        pass: String
    ): Result<AuthResult, String>
}

// Описывает интерфейс работы с хостами
interface HostService {
    val currentHost: String?
    fun changeHost(host: String)
    fun ping(host: String = currentHost.orEmpty()): Boolean
}

// Описывает интерфейс работы с сессией
interface SessionsService {
    val currentSession: Session?
    fun changeSession(session: Session)
}

// Описывает интерфейс доступа к потоку событий
interface EventsFlowProvider {
    fun eventsFlow(): Flow<Event>
}

class NPChatRepository(
    private val api: NPChatApi,
    private val localPrefs: NPChatLocalPrefs,
) {
    suspend fun isSessionActual(): Boolean {
        if (localPrefs.baseUrl.isBlank() || localPrefs.token.isBlank()) {
            return false
        }

        return api.me().isSuccess
    }

    suspend fun login(
        login: String,
        password: String,
        server: String
    ): kotlin.Result<ApiModel.User> {
        val resp = api.login(
            server = server,
            body = ApiModel.LoginBody(login, password)
        )

        return resp.map { it.user }
    }
}