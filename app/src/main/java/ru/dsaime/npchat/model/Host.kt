package ru.dsaime.npchat.model

data class Host(
    val url: String,
    val status: Status,
) {
    enum class Status {
        ONLINE,
        OFFLINE,
        INCOMPATIBLE,
        UNKNOWN,
    }
}
