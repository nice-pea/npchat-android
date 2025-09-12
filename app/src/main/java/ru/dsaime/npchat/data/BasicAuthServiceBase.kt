package ru.dsaime.npchat.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import ru.dsaime.npchat.model.Session
import ru.dsaime.npchat.model.User

// Реализует аутентификацию
class BasicAuthServiceBase(
    private val api: NPChatApi,
) : BasicAuthService {
    override suspend fun login(
        login: String,
        pass: String,
        host: String,
    ): Result<BasicAuthService.AuthResult, String> {
        val reqBody = ApiModel.LoginBody(login, pass)
        return api
            .login(host = host, body = reqBody)
            .mapCatching { Ok(it.toModel()) }
            .getOrElse { Err(it.toUserMessage()) }
    }

    override suspend fun registration(
        login: String,
        nick: String,
        name: String,
        pass: String,
        host: String,
    ): Result<BasicAuthService.AuthResult, String> {
        val reqBody =
            ApiModel.RegistrationBody(
                login = login,
                password = pass,
                name = name,
                nick = nick,
            )
        return api
            .registration(host = host, body = reqBody)
            .mapCatching { Ok(it.toModel()) }
            .getOrElse { Err(it.toUserMessage()) }
    }
}

fun ApiModel.AuthResp.toModel(): BasicAuthService.AuthResult =
    BasicAuthService.AuthResult(
        user =
            User(
                id = user.id,
                name = user.name,
                nick = user.nick,
            ),
        session =
            Session(
                id = session.id,
                name = session.name,
                status = session.status,
                refreshToken = session.refreshToken.token,
                refreshTokenExpiresAt = session.refreshToken.expiry,
                accessToken = session.accessToken.token,
                accessTokenExpiresAt = session.accessToken.expiry,
            ),
    )

fun Throwable.toUserMessage(): String = message.orEmpty().ifEmpty { "unknown error" }
