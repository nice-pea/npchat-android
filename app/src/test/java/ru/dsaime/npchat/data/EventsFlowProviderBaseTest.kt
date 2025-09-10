package ru.dsaime.npchat.data

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.dsaime.npchat.model.Session
import java.time.OffsetDateTime

class EventsFlowProviderBaseTest {
    @Test
    fun eventsFlow() {
        val session = Session(
            id = "",
            name = "",
            status = "",
            refreshToken = "",
            refreshTokenExpiresAt = OffsetDateTime.now(),
            accessToken = "d5d19d91-1248-4391-8004-606370a21720",
            accessTokenExpiresAt = OffsetDateTime.now()
        )
        runBlocking {
            EventsFlowProviderBase({ "https://api.npchat.dsaime.ru:443" })
                .eventsFlow(session)
                .collect {
                    println(it)
                }
            println("end")
        }
        assert(1 == 1)
    }

}