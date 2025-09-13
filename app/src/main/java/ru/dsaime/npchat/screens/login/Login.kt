package ru.dsaime.npchat.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.common.functions.ToastDuration
import ru.dsaime.npchat.common.functions.toast
import ru.dsaime.npchat.data.BasicAuthService
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.screens.home.ROUTE_HOME
import ru.dsaime.npchat.screens.registration.ROUTE_REGISTRATION
import ru.dsaime.npchat.ui.components.Button
import ru.dsaime.npchat.ui.components.Input
import ru.dsaime.npchat.ui.theme.Dp20
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

const val ROUTE_LOGIN = "Login"

@Composable
fun LoginScreenDestination(navController: NavController) {
    val vm = koinViewModel<LoginViewModel>()
    LoginScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::handleEvents,
        onNavigationRequest = {
            when (it) {
                LoginEffect.Navigation.ToHome -> navController.navigate(ROUTE_HOME)
//                LoginEffect.Navigation.ToOAuth -> navController.navigate(RouteOAuthLogin)
                LoginEffect.Navigation.ToRegistration -> navController.navigate(ROUTE_REGISTRATION)
                else -> {}
            }
        },
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
//                is SplashEffect.Navigation -> onNavigationRequested(effect)
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
            val host = state.host.value()
            Input(
                modifier = Modifier.weight(1f),
                title = "Сервер",
                placeholder = "http://example.com",
                value = host ?: state.host::class.simpleName!!,
                onValueChange = { onEventSent(LoginEvent.SetServer(it)) },
                enabled = host != null,
            )
            androidx.compose.material3.Button(
//                modifier = Modifier
//                    .background()
//                    .size(20.dp),
                onClick = { onEventSent(LoginEvent.CheckConn) },
            ) { Text(state.connStatus::class.simpleName ?: "null", color = White) }
        }
        Input(
            title = "Логин",
            placeholder = "Enter key for access to server",
            value = state.login,
            onValueChange = { onEventSent(LoginEvent.SetLogin(it)) },
        )
        Input(
            title = "Пароль",
            placeholder = "Enter key for access to server",
            value = state.password,
            onValueChange = { onEventSent(LoginEvent.SetPassword(it)) },
        )
        Button(
            onClick = { onEventSent(LoginEvent.Enter) },
            text = "Enter",
        )
        Button(
            onClick = { onEventSent(LoginEvent.GoToRegistration) },
            text = "Перейти к регистрации",
        )
        Button(
            onClick = { onEventSent(LoginEvent.GoToOAuth) },
            text = "Вход через сторонний сервис",
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
}

data class LoginState(
    val host: LoginHost = LoginHost.Loading,
    val connStatus: LoginConnStatus = LoginConnStatus.None,
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)

sealed interface LoginHost {
    data class Value(
        val text: String,
    ) : LoginHost

    object Loading : LoginHost

    fun value(): String? = (this as? Value)?.text
}

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
        object ToOAuth : Navigation

        object ToRegistration : Navigation

        object ToHome : Navigation
    }
}

class LoginViewModel(
    private val authService: BasicAuthService,
    private val hostService: HostService,
    private val sessionsService: SessionsService,
) : BaseViewModel<LoginEvent, LoginState, LoginEffect>() {
    init {
        viewModelScope.launch {
            delay(100)
            val prefHost = hostService.preferredHost()
            setState { copy(host = LoginHost.Value(prefHost.orEmpty())) }
        }
    }

    private suspend fun checkConn() {
        val host = viewState.value.host.value() ?: return
        val status =
            if (hostService.ping(host)) {
                LoginConnStatus.Ok
            } else {
                LoginConnStatus.Err
            }
        setState { copy(connStatus = status) }
    }

    private suspend fun enter() {
        val host =
            viewState.value.host.value() ?: run {
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
                LoginEffect.Navigation.ToHome.emit()
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
            LoginEvent.GoToOAuth -> LoginEffect.Navigation.ToOAuth.emit()
            LoginEvent.GoToRegistration -> LoginEffect.Navigation.ToRegistration.emit()
            is LoginEvent.SetLogin -> setState { copy(login = event.value) }
            is LoginEvent.SetPassword -> setState { copy(password = event.value) }
            is LoginEvent.SetServer -> setState { copy(host = LoginHost.Value(event.value)) }
        }
    }
}
