package ru.dsaime.npchat.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.common.base.eventHandler
import ru.dsaime.npchat.common.functions.ToastDuration
import ru.dsaime.npchat.common.functions.toast
import ru.dsaime.npchat.data.BasicAuthService
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.model.Host
import ru.dsaime.npchat.ui.components.Gap
import ru.dsaime.npchat.ui.components.HostSelect
import ru.dsaime.npchat.ui.components.Input
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.theme.Dp20
import ru.dsaime.npchat.ui.theme.Font
import ru.dsaime.npchat.ui.theme.White

@Preview(
    backgroundColor = 0xFF000000,
    showBackground = true,
)
@Composable
private fun PreviewLoginScreen() {
    LoginScreen(
        state = LoginState(),
        effectFlow = flow { },
        onEventSent = {},
        onNavigationRequest = {},
    )
}

@Composable
fun LoginScreenDestination(onNavigationRequest: (LoginEffect.Navigation) -> Unit) {
    val vm = koinViewModel<LoginViewModel>()
    LoginScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::setEvent,
        onNavigationRequest = onNavigationRequest,
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    effectFlow: Flow<LoginEffect>?,
    onEventSent: (LoginEvent) -> Unit,
    onNavigationRequest: (LoginEffect.Navigation) -> Unit,
) {
    val ctx = LocalContext.current
    LaunchedEffect(1) {
        effectFlow
            ?.onEach { effect ->
                when (effect) {
                    is LoginEffect.Navigation -> onNavigationRequest(effect)
                    is LoginEffect.ShowError -> toast(effect.msg, ctx, ToastDuration.LONG)
                }
            }?.collect()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(Dp20),
        verticalArrangement = Arrangement.Center,
    ) {
        Row {
            Input(
                modifier = Modifier.weight(1f),
                title = "Сервер",
                placeholder = "http://example.com",
                value = state.host,
                onValueChange = onEventSent.eventHandler(LoginEvent::SetServer),
                enabled = state.hostEnabled,
            )
            Button(onEventSent.eventHandler(LoginEvent.CheckConn)) {
                Text(state.connStatus::class.simpleName ?: "null", color = White)
            }
        }
        val fakeHosts =
            listOf(
                Host(url = "https://main.example.com:7877", status = Host.Status.ONLINE),
                Host(url = "https://test.example.com/api", status = Host.Status.OFFLINE),
                Host(url = "https://cloud.example.com/v2", status = Host.Status.INCOMPATIBLE),
                Host(url = "https://api.example.com", status = Host.Status.UNKNOWN),
                null
            )
        fakeHosts.forEach {
            HostSelect(
                host = it,
                onClick = {},
                onCheckConn = {},
            )
        }
        HostSelect(
            host = Host(url = state.host, status = Host.Status.ONLINE),
            onClick = {},
            onCheckConn = {},
        )
        Input(
            title = "Логин",
            placeholder = "",
            value = state.login,
            onValueChange = onEventSent.eventHandler(LoginEvent::SetLogin),
        )
        Input(
            title = "Пароль",
            placeholder = "",
            value = state.password,
            onValueChange = onEventSent.eventHandler(LoginEvent::SetPassword),
        )
        Gap(20.dp)
        LeftButton(
            text = "Войти",
            onClick = onEventSent.eventHandler(LoginEvent.Enter),
            isRight = true,
        )
        LeftButton(
            text = "Перейти к регистрации",
            onClick = onEventSent.eventHandler(LoginEvent.GoToRegistration),
        )
        Gap(20.dp)
        Text("или", style = Font.Text16W400)
        Gap(20.dp)
        LeftButton(
            text = "Вход через сторонний сервис",
            onClick = onEventSent.eventHandler(LoginEvent.GoToOAuth),
        )
    }
}

sealed interface LoginEvent {
    object CheckConn : LoginEvent

    object Enter : LoginEvent

    object GoToRegistration : LoginEvent

    object GoToOAuth : LoginEvent

    class SetServer(
        val value: String,
    ) : LoginEvent

    class SetLogin(
        val value: String,
    ) : LoginEvent

    class SetPassword(
        val value: String,
    ) : LoginEvent

    object GoToTest : LoginEvent
}

data class LoginState(
    val host: String = "",
    val hostEnabled: Boolean = false,
    val connStatus: LoginConnStatus = LoginConnStatus.None,
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)

sealed interface LoginConnStatus {
    object Ok : LoginConnStatus

    object Err : LoginConnStatus

    object None : LoginConnStatus
}

sealed interface LoginEffect {
    data class ShowError(
        val msg: String,
    ) : LoginEffect

    sealed interface Navigation : LoginEffect {
        object Test : Navigation

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
    init {
        viewModelScope.launch {
            val prefHost = hostService.preferredHost()
            setState { copy(host = prefHost.orEmpty()) }
        }
    }

    private suspend fun checkConn() {
        val status =
            if (hostService.ping(viewState.value.host)) {
                LoginConnStatus.Ok
            } else {
                LoginConnStatus.Err
            }
        setState { copy(connStatus = status) }
    }

    private suspend fun enter() {
        val host =
            viewState.value.host.ifBlank {
                LoginEffect.ShowError("host не установлен").emit()
                return
            }

        if (viewState.value.connStatus == LoginConnStatus.None) {
            checkConn()
        }
        if (viewState.value.connStatus == LoginConnStatus.Err) {
            LoginEffect.ShowError("нет соединения с сервером").emit()
            return
        }

        authService
            .login(
                login = viewState.value.login,
                pass = viewState.value.password,
                host = host,
            ).onSuccess {
                sessionsService.changeSession(it.session)
                hostService.changeHost(host)
                LoginEffect.Navigation.Home.emit()
            }.onFailure { message ->
                LoginEffect.ShowError(message).emit()
            }
    }

    //    override fun setInitialState(): LoginState = LoginState()
    override fun setInitialState(): LoginState = LoginState()

    override fun handleEvents(event: LoginEvent) {
        when (event) {
            LoginEvent.CheckConn -> viewModelScope.launch { checkConn() }
            LoginEvent.Enter -> viewModelScope.launch { enter() }
            LoginEvent.GoToOAuth -> LoginEffect.Navigation.OAuth.emit()
            LoginEvent.GoToRegistration -> LoginEffect.Navigation.Registration.emit()
            is LoginEvent.SetLogin -> setState { copy(login = event.value) }
            is LoginEvent.SetPassword -> setState { copy(password = event.value) }
            is LoginEvent.SetServer -> setState { copy(host = event.value) }
            LoginEvent.GoToTest -> LoginEffect.Navigation.Test.emit()
        }
    }
}
