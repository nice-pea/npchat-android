package ru.dsaime.npchat.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.sse.SSE
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.sse.sse
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.dsaime.npchat.model.Event
import ru.dsaime.npchat.model.Session
import ru.dsaime.npchat.network.retroGson
import java.time.OffsetDateTime
import kotlin.test.assertEquals

class EventsFlowProviderKtorSSETest {
    @Test
    fun eventsFlow() {
        val session =
            Session(
                id = "",
                name = "",
                status = "",
                refreshToken = "",
                refreshTokenExpiresAt = OffsetDateTime.now(),
                accessToken = "d5d19d91-1248-4391-8004-606370a21720",
                accessTokenExpiresAt = OffsetDateTime.now(),
            )
        val expectedEvents =
            listOf(
                Event("qwr", OffsetDateTime.now(), mapOf("a" to 1, "b" to 2)),
                Event("zcv", OffsetDateTime.now(), mapOf("a" to 1, "b" to 2)),
            )

        runBlocking {
            val server =
                embeddedServer(Netty, 0) {
                    install(io.ktor.server.sse.SSE)
                    routing {
                        sse("/events") {
                            expectedEvents.forEach {
                                send(retroGson.toJson(it), "event")
                            }
                        }
                    }
                }.start(false)

            // Получить порт сервера
            val port =
                server.application.engine
                    .resolvedConnectors()
                    .first()
                    .port

            // Создать клиент
            val client =
                HttpClient(OkHttp) {
                    install(SSE)
                }

            // Сохранить все события в список
            val receivedEvents =
                EventsFlowProviderKtorSSE(client, { "http://localhost:$port" })
                    .eventsFlow(session)
                    .toList()

            // Остановить сервер
            server.stop()
            assertEquals(expectedEvents.size, receivedEvents.size)
        }
    }
}
