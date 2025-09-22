package ru.dsaime.npchat.screens.home

import ru.dsaime.npchat.common.base.BaseViewModel

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

class HomeViewModel : BaseViewModel<HomeEvent, HomeState, HomeEffect>() {
    override fun setInitialState() = HomeState

    override fun handleEvents(event: HomeEvent) {
        when (event) {
            HomeEvent.NavChats -> HomeEffect.Navigation.Chats.emit()
            HomeEvent.NavControl -> HomeEffect.Navigation.Control.emit()
        }
    }
}
