package ru.dsaime.npchat.screens.splash

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.dsaime.npchat.base.BaseViewModel
import ru.dsaime.npchat.base.ViewEvent
import ru.dsaime.npchat.base.ViewSideEffect
import ru.dsaime.npchat.base.ViewState
import ru.dsaime.npchat.data.AuthService
import ru.dsaime.npchat.data.SessionsService

sealed interface SplashEvent : ViewEvent {
    object CheckSession : SplashEvent
}

object SplashState : ViewState

sealed interface SplashEffect : ViewSideEffect {
//        data class ShowError(val msg: String) : Effect

    sealed interface Navigation : SplashEffect {
        object ToLogin : Navigation

        object ToHome : Navigation
    }
}

class SplashViewModel(
    private val sessionsService: SessionsService,
    private val repo: AuthService,
) : BaseViewModel<SplashEvent, SplashState, SplashEffect>() {
    override fun setInitialState() = SplashState

    override fun handleEvents(event: SplashEvent) {
        when (event) {
            SplashEvent.CheckSession ->
                viewModelScope.launch {
                    val current = sessionsService.currentSession()
                    if (current == null || !sessionsService.sessionIsActual(current)) {
                        setEffect { SplashEffect.Navigation.ToLogin }
                    } else {
                        setEffect { SplashEffect.Navigation.ToHome }
                    }
                }
        }
    }
}
