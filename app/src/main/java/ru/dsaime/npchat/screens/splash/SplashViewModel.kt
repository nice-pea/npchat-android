package ru.dsaime.npchat.screens.splash

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.SessionsService

sealed interface SplashEvent {
    object CheckSession : SplashEvent
}

object SplashState

sealed interface SplashEffect {
//        data class ShowError(val msg: String) : Effect

    sealed interface Navigation : SplashEffect {
        object ToLogin : Navigation

        object ToHome : Navigation
    }
}

class SplashViewModel(
    private val sessionsService: SessionsService,
) : BaseViewModel<SplashEvent, SplashState, SplashEffect>() {
    override fun setInitialState() = SplashState

    override fun handleEvents(event: SplashEvent) {
        when (event) {
            SplashEvent.CheckSession ->
                viewModelScope.launch {
                    val current =
                        sessionsService.currentSession() ?: run {
                            // Если нет сохраненной сессии, перейти на экран логина
                            SplashEffect.Navigation.ToLogin.emit()
                            return@launch
                        }

                    if (sessionsService.isActual(current) || sessionsService.refresh(current)) {
                        // Если сессия актива, прейти на Главный экран
                        SplashEffect.Navigation.ToHome.emit()
                    } else {
                        SplashEffect.Navigation.ToLogin.emit()
                    }
                }
        }
    }
}
