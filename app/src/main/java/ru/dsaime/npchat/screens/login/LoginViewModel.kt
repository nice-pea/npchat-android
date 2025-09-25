package ru.dsaime.npchat.screens.login

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.BasicAuthService
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.model.Host

sealed interface LoginEvent {
    object SelectHost : LoginEvent

    object Enter : LoginEvent

    object GoToRegistration : LoginEvent

    object GoToOAuth : LoginEvent

    class SetLogin(
        val value: String,
    ) : LoginEvent

    class SetPassword(
        val value: String,
    ) : LoginEvent
}

data class LoginState(
    val host: Host? = null,
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)

sealed interface LoginEffect {
    data class ShowError(
        val msg: String,
    ) : LoginEffect

    sealed interface Navigation : LoginEffect {
        object HostSelect : Navigation

        object OAuth : Navigation

        object Registration : Navigation

        object Home : Navigation
    }
}

class LoginViewModel(
    private val authService: BasicAuthService,
    private val hostService: HostService,
    private val sessionsService: SessionsService,
) : BaseViewModel<LoginEvent, LoginState, LoginEffect>() {
    override fun setInitialState(): LoginState = LoginState()

    init {
        viewModelScope.launch {
            subscribeToHostChanges()
        }
    }

    // Обновляет хост при каждом изменении currentHost
    private suspend fun subscribeToHostChanges() {
        hostService.currentHostFlow().collectLatest { host ->
            setState { copy(host = host) }
        }
    }

    private suspend fun enter() {
        val host = viewState.value.host
        if (host == null || host.url.isBlank()) {
            LoginEffect.ShowError("host не установлен").emit()
            return
        }

        if (host.status != Host.Status.ONLINE) {
            LoginEffect.ShowError("нет соединения с сервером").emit()
            return
        }

        authService
            .login(
                login = viewState.value.login,
                pass = viewState.value.password,
                host = host.url,
            ).onSuccess {
                sessionsService.changeSession(it.session)
                hostService.changeHost(host)
                LoginEffect.Navigation.Home.emit()
            }.onFailure { message ->
                LoginEffect.ShowError(message).emit()
            }
    }

    override fun handleEvents(event: LoginEvent) {
        when (event) {
            LoginEvent.Enter -> viewModelScope.launch { enter() }
            LoginEvent.GoToOAuth -> LoginEffect.Navigation.OAuth.emit()
            LoginEvent.GoToRegistration -> LoginEffect.Navigation.Registration.emit()
            is LoginEvent.SetLogin -> setState { copy(login = event.value) }
            is LoginEvent.SetPassword -> setState { copy(password = event.value) }
            LoginEvent.SelectHost -> LoginEffect.Navigation.HostSelect.emit()
        }
    }
}
