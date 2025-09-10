package ru.dsaime.npchat.screens.splash

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.dsaime.npchat.base.BaseViewModel
import ru.dsaime.npchat.base.ViewEvent
import ru.dsaime.npchat.base.ViewSideEffect
import ru.dsaime.npchat.base.ViewState
import ru.dsaime.npchat.data.AuthServiceBase
import ru.dsaime.npchat.data.SessionsService

class SplashContract {
    sealed interface Event : ViewEvent {
        object CheckSession : Event
    }

    object State : ViewState

    sealed interface Effect : ViewSideEffect {
//        data class ShowError(val msg: String) : Effect

        sealed interface Navigation : Effect {
            object ToLogin : Navigation
            object ToHome : Navigation
        }
    }
}

class SplashViewModel(
    private val sessionsService: SessionsService,
    private val repo: AuthServiceBase,
) : BaseViewModel<SplashContract.Event,
        SplashContract.State,
        SplashContract.Effect>() {

    override fun setInitialState() = SplashContract.State

    override fun handleEvents(event: SplashContract.Event) {
        when (event) {
            SplashContract.Event.CheckSession -> viewModelScope.launch {
                val current = sessionsService.currentSession()
                if (current == null || !sessionsService.sessionIsActual(current))
                    setEffect { SplashContract.Effect.Navigation.ToHome }
                else
                    setEffect { SplashContract.Effect.Navigation.ToLogin }
            }
        }
    }
}
