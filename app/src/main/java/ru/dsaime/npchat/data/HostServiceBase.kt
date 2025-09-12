package ru.dsaime.npchat.data

class HostServiceBase(
    private val api: NPChatApi,
) : HostService {
    private var currentHost: String? = null

    override fun currentHost() = currentHost

    override fun changeHost(host: String) {
        currentHost = host
    }

    override fun wellKnown(): List<String> =
        listOf(
            "https://api.npchat.dsaime.ru:443",
            "http://localhost:8080",
        )

    override suspend fun ping(host: String) = api.ping(host).isSuccess
}
