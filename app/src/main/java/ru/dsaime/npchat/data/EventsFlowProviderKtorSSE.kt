package ru.dsaime.npchat.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.url
import kotlinx.coroutines.flow.flow
import ru.dsaime.npchat.model.Event
import ru.dsaime.npchat.model.Session
import ru.dsaime.npchat.network.BaseUrlProvider
import ru.dsaime.npchat.network.retroGson

class EventsFlowProviderKtorSSE(
    private val client: HttpClient,
    private val baseUrlProvider: BaseUrlProvider,
) : EventsFlowProvider {
    override suspend fun eventsFlow(session: Session) =
        flow<Event> {
            client.sse({
                url("${baseUrlProvider.baseUrl()}/events")
                headers["Authorization"] = "Bearer ${session.accessToken}"
            }) {
                incoming.collect { event ->
                    when (event.event) {
                        "keepalive" -> println("keepalive event is here")
                        "error" -> error(event.data ?: "received error event")
                        "event" -> emit(retroGson.fromJson(event.data, Event::class.java))
                    }
                }
            }
        }
}
