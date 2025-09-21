package ru.dsaime.npchat.screens.registration

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
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.common.functions.ToastDuration
import ru.dsaime.npchat.common.functions.toast
import ru.dsaime.npchat.data.BasicAuthService
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.screens.login.LoginConnStatus
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.Input
import ru.dsaime.npchat.ui.theme.Dp20
import ru.dsaime.npchat.ui.theme.White

@Composable
fun RegistrationScreenDestination(onNavigationRequest: (RegistrationEffect.Navigation) -> Unit) {
    val vm = koinViewModel<RegistrationViewModel>()
    RegistrationScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::handleEvents,
        onNavigationRequest = onNavigationRequest,
    )
}

@Composable
fun RegistrationScreen(
    state: RegistrationState,
    effectFlow: Flow<RegistrationEffect>?,
    onEventSent: (RegistrationEvent) -> Unit,
    onNavigationRequest: (RegistrationEffect.Navigation) -> Unit,
) {
    val ctx = LocalContext.current
    LaunchedEffect(1) {
        effectFlow
            ?.onEach { effect ->
                when (effect) {
//                is SplashEffect.Navigation -> onNavigationRequested(effect)
                    is RegistrationEffect.Navigation -> onNavigationRequest(effect)
                    is RegistrationEffect.ShowError -> toast(effect.msg, ctx, ToastDuration.LONG)
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
                onValueChange = { onEventSent(RegistrationEvent.SetHost(it)) },
                enabled = state.hostEnabled,
            )
            androidx.compose.material3.Button(
                onClick = { onEventSent(RegistrationEvent.CheckConn) },
            ) { Text(state.connStatus::class.simpleName ?: "null", color = White) }
        }
        Input(
            title = "Видимое имя",
            placeholder = "",
            value = state.name,
            onValueChange = { onEventSent(RegistrationEvent.SetName(it)) },
        )
        Input(
            title = "Ник",
            placeholder = "",
            value = state.nick,
            onValueChange = { onEventSent(RegistrationEvent.SetNick(it)) },
        )
        Input(
            title = "Логин",
            placeholder = "Enter key for access to server",
            value = state.login,
            onValueChange = { onEventSent(RegistrationEvent.SetLogin(it)) },
        )
        Input(
            title = "Пароль",
            placeholder = "Enter key for access to server",
            value = state.password,
            onValueChange = { onEventSent(RegistrationEvent.SetPassword(it)) },
        )
        LeftButton(
            onClick = { onEventSent(RegistrationEvent.Confirm) },
            text = "Регистрация",
        )
    }
}

sealed interface RegistrationEvent {
    class SetHost(
        val value: String,
    ) : RegistrationEvent

    object Confirm : RegistrationEvent

    object CheckConn : RegistrationEvent

    class SetNick(
        val value: String,
    ) : RegistrationEvent

    class SetName(
        val value: String,
    ) : RegistrationEvent

    class SetLogin(
        val value: String,
    ) : RegistrationEvent

    class SetPassword(
        val value: String,
    ) : RegistrationEvent
}

data class RegistrationState(
    val host: String = "",
    val hostEnabled: Boolean = false,
    val connStatus: RegistrationConnStatus = RegistrationConnStatus.None,
    val name: String = "",
    val nick: String = "",
    val login: String = "",
    val password: String = "",
)

sealed interface RegistrationConnStatus {
    object Ok : RegistrationConnStatus

    object Err : RegistrationConnStatus

    object None : RegistrationConnStatus
}

sealed interface RegistrationEffect {
    data class ShowError(
        val msg: String,
    ) : RegistrationEffect

    sealed interface Navigation : RegistrationEffect {
        object ToHome : Navigation
    }
}

class RegistrationViewModel(
    private val authService: BasicAuthService,
    private val hostService: HostService,
    private val sessionsService: SessionsService,
) : BaseViewModel<RegistrationEvent, RegistrationState, RegistrationEffect>() {
    init {
        viewModelScope
            .launch {
                val prefHost = hostService.preferredHost()
                setState { copy(host = prefHost.orEmpty()) }
            }.invokeOnCompletion {
                setState { copy(hostEnabled = true) }
            }
    }

    override fun setInitialState() = RegistrationState()

    private suspend fun checkConn() {
        val status =
            if (hostService.ping(viewState.value.host)) {
                RegistrationConnStatus.Ok
            } else {
                RegistrationConnStatus.Err
            }
        setState { copy(connStatus = status) }
    }

    private suspend fun confirm() {
        val host =
            viewState.value.host.ifBlank {
                RegistrationEffect.ShowError("host не установлен").emit()
                return
            }

        if (viewState.value.connStatus == LoginConnStatus.None) {
            checkConn()
        }
        if (viewState.value.connStatus == LoginConnStatus.Err) {
            RegistrationEffect.ShowError("нет соединения с сервером").emit()
            return
        }

        authService
            .registration(
                name = viewState.value.name,
                nick = viewState.value.nick,
                login = viewState.value.login,
                pass = viewState.value.password,
                host = host,
            ).onSuccess {
                sessionsService.changeSession(it.session)
                hostService.changeHost(host)
                RegistrationEffect.Navigation.ToHome.emit()
            }.onFailure { message ->
                RegistrationEffect.ShowError(message).emit()
            }
    }

    override fun handleEvents(event: RegistrationEvent) {
        when (event) {
            RegistrationEvent.CheckConn -> viewModelScope.launch { checkConn() }
            RegistrationEvent.Confirm -> viewModelScope.launch { confirm() }
            is RegistrationEvent.SetHost -> setState { copy(host = event.value) }
            is RegistrationEvent.SetLogin -> setState { copy(login = event.value) }
            is RegistrationEvent.SetName -> setState { copy(name = event.value) }
            is RegistrationEvent.SetNick -> setState { copy(nick = event.value) }
            is RegistrationEvent.SetPassword -> setState { copy(password = event.value) }
        }
    }
}
