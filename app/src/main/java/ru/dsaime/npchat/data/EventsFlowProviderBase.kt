package ru.dsaime.npchat.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.deserialize
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.url
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import ru.dsaime.npchat.model.Event
import ru.dsaime.npchat.model.Session
import ru.dsaime.npchat.network.BaseUrlProvider

class EventsFlowProviderBase(
    private val baseUrlProvider: BaseUrlProvider,
) : EventsFlowProvider {
    private val client = HttpClient {
        install(SSE)
    }

    override suspend fun eventsFlow(session: Session) = flow {
        client.sse(
            request = { url("${baseUrlProvider.baseUrl()}/events") },
            deserialize = { typeInfo, jsonString ->
                val serializer = Json.serializersModule.serializer(typeInfo.kotlinType!!)
                Json.decodeFromString(serializer, jsonString)!!
            }
        ) {
            incoming.collect { event ->
                when (event.event) {
                    "keepalive" -> println("keepalive event is here")
                    "error" -> error(event.data ?: "received error event")
                    "event" -> deserialize<Event>(event.data)?.let { emit(it) }
                }
            }
        }
    }
}
