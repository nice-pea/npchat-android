package ru.dsaime.npchat.screens.home

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.EventsFlowProvider
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.network.retroGson

sealed interface HomeEvent {
    object NavChats : HomeEvent

    object NavControl : HomeEvent
}

object HomeState

sealed interface HomeEffect {
    sealed interface Navigation : HomeEffect {
        object Control : Navigation

        object Chats : Navigation
    }
}

class HomeViewModel(
    val eventsFlowProvider: EventsFlowProvider,
    val sessionsService: SessionsService,
) : BaseViewModel<HomeEvent, HomeState, HomeEffect>() {
    override fun setInitialState() = HomeState

    init {
        viewModelScope.launch {
            val session =
                sessionsService.currentSession()
                    ?: error("No current session")
            eventsFlowProvider
                .eventsFlow(session)
                .collect { event ->
                    event
                        .onSuccess {
                            val event = it
                            println(retroGson.toJson(event))
                        }.onFailure {
                            println(it)
                        }
                }
        }
    }

    override fun handleEvents(event: HomeEvent) {
        when (event) {
            HomeEvent.NavChats -> HomeEffect.Navigation.Chats.emit()
            HomeEvent.NavControl -> HomeEffect.Navigation.Control.emit()
        }
    }
}
