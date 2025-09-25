package ru.dsaime.npchat.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.dsaime.npchat.common.functions.runSuspend
import ru.dsaime.npchat.model.Event
import ru.dsaime.npchat.model.Session
import ru.dsaime.npchat.network.BaseUrlProvider
import ru.dsaime.npchat.network.retroGson
import java.time.OffsetDateTime

class EventsFlowProviderKtorSSE(
    private val client: HttpClient,
    private val baseUrlProvider: BaseUrlProvider,
) : EventsFlowProvider {
    override suspend fun eventsFlow(session: Session): Flow<Result<Event, String>> =
        flow {
            client.sse({
                url("${baseUrlProvider.baseUrl()}/events")
                headers["Authorization"] = "SessionToken ${session.accessToken}"
            }) {
                incoming.collect { event ->
                    when (event.event) {
                        "error" -> Err(event.data ?: "received error event")
                        "event" ->
                            retroGson
                                .fromJson(event.data, RawEvent::class.java)
                                .toEvent()
                                .runSuspend(::emit)
                    }
                }
            }
        }
}

private inline fun <reified T : Any> RawEvent.prop(name: String): T = retroGson.fromJson(retroGson.toJson(data[name]), T::class.java)

private fun RawEvent.toEvent() =
    when (type) {
        Event.ParticipantAdded.NAME -> {
            Event.ParticipantAdded(
                chat = prop<ApiModel.Chat>("chat").toModel(),
                participant = prop<ApiModel.Participant>("participant").toModel(),
            )
        }

        Event.ParticipantRemoved.NAME -> {
            Event.ParticipantRemoved(
                chat = prop<ApiModel.Chat>("chat").toModel(),
                participant = prop<ApiModel.Participant>("participant").toModel(),
            )
        }

        Event.ChatCreated.NAME ->
            Event.ChatCreated(
                chat = prop<ApiModel.Chat>("chat").toModel(),
            )

        Event.ChatUpdated.NAME ->
            Event.ChatUpdated(
                chat = prop<ApiModel.Chat>("chat").toModel(),
            )

        else -> null
    }.toResultOr { "received unknown event" }

data class RawEvent(
    @SerializedName("Type") val type: String, // Тип события
    @SerializedName("CreatedIn") val createdIn: OffsetDateTime, // Время создания
    @SerializedName("Data") val data: Map<String, Any>, // Полезная нагрузка
)
