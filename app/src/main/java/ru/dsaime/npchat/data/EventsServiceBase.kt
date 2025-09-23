package ru.dsaime.npchat.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import ru.dsaime.npchat.model.Event

class EventsServiceBase(
    private val eventsFlowProvider: EventsFlowProvider,
    private val sessionsService: SessionsService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : EventsService {
    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsFlow: SharedFlow<Result<Event, String>> =
        sessionsService
            .currentSessionFlow()
            .transformLatest { session ->
                if (session == null) {
                    emit(Err("eventsFlow: No session"))
                } else {
                    emitAll(eventsFlowProvider.eventsFlow(session))
                }
            }.shareIn(coroutineScope, SharingStarted.Eagerly)

    override fun onParticipantAdded() = filterEvents<Event.ParticipantAdded>()

    override fun onParticipantRemoved() = filterEvents<Event.ParticipantRemoved>()

    override fun onChatNameUpdated() = filterEvents<Event.ChatNameUpdated>()

    override fun onChatCreated() = filterEvents<Event.ChatCreated>()

    private inline fun <reified V2> filterEvents(): Flow<Result<V2, String>> =
        eventsFlow
            .filter { it.isErr || it.get() is V2 }
            .map { it.map { it as V2 } }
}
