package ru.dsaime.npchat.data

fun Throwable.toUserMessage(): String = message.orEmpty().ifEmpty { "unknown error" }
