package ru.dsaime.npchat.data

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

    suspend fun login(login: String, password: String, server: String): Result<ApiModel.User> {
        val resp = api.login(
            server = server,
            body = ApiModel.LoginBody(login, password)
        )

        return resp.map { it.user }
    }
}