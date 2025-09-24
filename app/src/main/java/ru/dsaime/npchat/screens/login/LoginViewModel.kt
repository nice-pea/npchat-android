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
    object CheckConn : LoginEvent

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
        hostService.currentBaseUrlFlow().collectLatest { baseUrl ->
            if (baseUrl == null || baseUrl.isBlank()) {
                setState { copy(host = null) }
            } else {
                setState { copy(host = Host(baseUrl, Host.Status.UNKNOWN)) }
                // Обновлять статус хоста
//                if (baseUrl = null) {
                hostService.statusFlow(baseUrl).collectLatest { status ->
                    setState { copy(host = host?.copy(status = status)) }
                }
//                }
            }
        }
    }

    // Проверяет подключение к серверу и обновляет отображаемое состояние
    private suspend fun checkConn() {
//        val host =
//            viewState.value.host ?: run {
//                setState { copy(connStatus = LoginConnStatus.None) }
//                return // Если хост не установлен, выходим
//            }
//        val pingResult = hostService.ping(host.url)
//        val status = if (pingResult) LoginConnStatus.Ok else LoginConnStatus.Err
//        setState { copy(connStatus = status) }
    }

    private suspend fun enter() {
        val hostUrl =
            viewState.value.host?.url.orEmpty().ifBlank {
                LoginEffect.ShowError("host не установлен").emit()
                return
            }

        if (viewState.value.host?.status != Host.Status.ONLINE) {
            LoginEffect.ShowError("нет соединения с сервером").emit()
            return
        }

        authService
            .login(
                login = viewState.value.login,
                pass = viewState.value.password,
                host = hostUrl,
            ).onSuccess {
                sessionsService.changeSession(it.session)
                hostService.changeBaseUrl(hostUrl)
                LoginEffect.Navigation.Home.emit()
            }.onFailure { message ->
                LoginEffect.ShowError(message).emit()
            }
    }

    //    override fun setInitialState(): LoginState = LoginState()

    override fun handleEvents(event: LoginEvent) {
        when (event) {
            LoginEvent.CheckConn -> viewModelScope.launch { checkConn() }
            LoginEvent.Enter -> viewModelScope.launch { enter() }
            LoginEvent.GoToOAuth -> LoginEffect.Navigation.OAuth.emit()
            LoginEvent.GoToRegistration -> LoginEffect.Navigation.Registration.emit()
            is LoginEvent.SetLogin -> setState { copy(login = event.value) }
            is LoginEvent.SetPassword -> setState { copy(password = event.value) }
            LoginEvent.SelectHost -> LoginEffect.Navigation.HostSelect.emit()
        }
    }
}
