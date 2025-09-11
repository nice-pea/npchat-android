package ru.dsaime.npchat.data

import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.sse.SSE
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.sse.send
import io.ktor.server.sse.sse
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import org.junit.Test
import ru.dsaime.npchat.model.Event
import ru.dsaime.npchat.model.Session
import ru.dsaime.npchat.network.retroGson
import java.time.OffsetDateTime
import kotlin.test.assertEquals

data class Gsoned(
    @SerializedName("CreatedIn") val createdIn: OffsetDateTime, // Время создания
)

class EventsFlowProviderKtorSSETest {
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
        val expectedEvents = listOf(
            Event(
                "qwr",
                OffsetDateTime.now(),
                JsonObject(mapOf("a" to JsonPrimitive(1), "b" to JsonPrimitive(2))),
            ),
            Event(
                "zcv",
                OffsetDateTime.now(),
                JsonObject(mapOf("a" to JsonPrimitive(1), "b" to JsonPrimitive(2))),
            ),
        )

        println(retroGson.toJson(Gsoned(OffsetDateTime.now())))
        runBlocking {
            val server = embeddedServer(Netty, 38925) {
                install(io.ktor.server.sse.SSE)
                routing {
                    sse("/events", serialize = { typeInfo, it ->
                        val serializer = Json.serializersModule.serializer(typeInfo.kotlinType!!)
                        Json.encodeToString(serializer, it)
                    }) {
                        expectedEvents.forEach {
                            send(data = it, event = "event")
                        }
                    }
                }
            }.start(false)

            val connector = server.application.engine.resolvedConnectors().first()

            val client = HttpClient(OkHttp) {
                install(SSE)
            }

            val receivedEvents =
                EventsFlowProviderKtorSSE(client, { "http://localhost:${connector.port}" })
                    .eventsFlow(session)
                    .toList()

            server.stop()
            assertEquals(expectedEvents.size, receivedEvents.size)
        }
    }

}