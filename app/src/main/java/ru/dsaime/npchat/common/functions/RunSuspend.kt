package ru.dsaime.npchat.common.functions

suspend inline fun <T, R> T.runSuspend(crossinline block: suspend T.() -> R): R = block()
