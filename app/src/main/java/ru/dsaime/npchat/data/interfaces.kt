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
    fun currentHost(): String?
    fun changeHost(host: String)
    suspend fun ping(host: String = currentHost().orEmpty()): Boolean
}

// Описывает интерфейс работы с сессией
interface SessionsService {
    fun currentSession(): Session?
    fun changeSession(session: Session)
    suspend fun sessionIsActual(session: Session): Boolean
}

// Описывает интерфейс доступа к потоку событий
interface EventsFlowProvider {
    fun eventsFlow(session: Session): Flow<Event>
}