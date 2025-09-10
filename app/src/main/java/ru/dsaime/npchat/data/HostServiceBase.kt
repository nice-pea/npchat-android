package ru.dsaime.npchat.data

class HostServiceBase(
    private val apiDyn: NPChatApi
) : HostService {

    private var currentHost: String? = null

    override fun currentHost() = currentHost

    override fun changeHost(host: String) {
        currentHost = host
    }

    override suspend fun ping(host: String): Boolean {
        return apiDyn.ping(host).isSuccess
    }
}