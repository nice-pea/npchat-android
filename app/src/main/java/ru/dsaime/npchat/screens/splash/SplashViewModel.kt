package ru.dsaime.npchat.screens.splash

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.model.Host

sealed interface SplashEvent {
    object CheckSession : SplashEvent
}

object SplashState

sealed interface SplashEffect {
//        data class ShowError(val msg: String) : Effect

    sealed interface Navigation : SplashEffect {
        object Login : Navigation

        object Home : Navigation
    }
}

class SplashViewModel(
    private val sessionsService: SessionsService,
    private val hostService: HostService,
) : BaseViewModel<SplashEvent, SplashState, SplashEffect>() {
    override fun setInitialState() = SplashState

    override fun handleEvents(event: SplashEvent) {
        when (event) {
            SplashEvent.CheckSession ->
                viewModelScope.launch {
                    // Если нет соединения с сервером или он не выбран, перейти на экран логина
                    val host = hostService.currentBaseUrl()
                    if (host == null || hostService.status(host) != Host.Status.ONLINE) {
                        SplashEffect.Navigation.Login.emit()
                        return@launch
                    }

                    // Если нет сохраненной сессии или она неактуальна, перейти на экран логина
                    val session = sessionsService.currentSession()
                    if (session == null || !(sessionsService.isActual(session) || sessionsService.refresh(session))) {
                        SplashEffect.Navigation.Login.emit()
                        return@launch
                    }

                    SplashEffect.Navigation.Home.emit()
                }
        }
    }
}
