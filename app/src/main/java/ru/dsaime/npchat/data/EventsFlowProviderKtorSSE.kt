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

private fun RawEvent.toEvent() =
    when (type) {
        Event.ParticipantAdded.NAME -> {
            val chatId = data["chat_id"] as String
            val participant = retroGson.toJson(data["participant"])

            Event.ParticipantAdded(
                chatId = chatId,
                participant =
                    retroGson
                        .fromJson(participant, ApiModel.Participant::class.java)
                        .toModel(),
            )
        }

        Event.ParticipantRemoved.NAME -> {
            val chatId = data["chat_id"] as String
            val participant = retroGson.toJson(data["participant"])

            Event.ParticipantRemoved(
                chatId = chatId,
                participant =
                    retroGson
                        .fromJson(participant, ApiModel.Participant::class.java)
                        .toModel(),
            )
        }

        Event.ChatCreated.NAME ->
            Event.ChatCreated(
                chatId = data["chat_id"] as String,
            )

        Event.ChatNameUpdated.NAME ->
            Event.ChatNameUpdated(
                chatId = data["chat_id"] as String,
                name = data["name"] as String,
            )

        else -> null
    }.toResultOr { "received unknown event" }

data class RawEvent(
    @SerializedName("Type") val type: String, // Тип события
    @SerializedName("CreatedIn") val createdIn: OffsetDateTime, // Время создания
    @SerializedName("Data") val data: Map<String, Any>, // Полезная нагрузка
)
