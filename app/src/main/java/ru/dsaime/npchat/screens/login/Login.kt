package ru.dsaime.npchat.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.base.BaseViewModel
import ru.dsaime.npchat.base.ViewEvent
import ru.dsaime.npchat.base.ViewSideEffect
import ru.dsaime.npchat.base.ViewState
import ru.dsaime.npchat.common.functions.ToastDuration
import ru.dsaime.npchat.common.functions.toast
import ru.dsaime.npchat.data.NPChatRepository
import ru.dsaime.npchat.screens.chats.RouteChats
import ru.dsaime.npchat.ui.components.Button
import ru.dsaime.npchat.ui.components.Input
import ru.dsaime.npchat.ui.theme.BlueSky
import ru.dsaime.npchat.ui.theme.Copper
import ru.dsaime.npchat.ui.theme.Dp20
import ru.dsaime.npchat.ui.theme.Olive
import ru.dsaime.npchat.ui.theme.Pink


@Preview(
    backgroundColor = 0xFF000000,
    showBackground = true,
)
@Composable
private fun PreviewLoginScreen() {
    LoginScreen(
        state = LoginContract.State(),
        effectFlow = flow { },
        onEventSent = {},
        onNavigationRequested = {}
    )
}

const val RouteLogin = "Login"


@Composable
fun LoginScreenDestination(
    navController: NavController,
) {
    val vm = koinViewModel<LoginViewModel>()
    LoginScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::handleEvents,
        onNavigationRequested = {
            when (it) {
                LoginContract.Effect.Navigation.ToHome -> navController.navigate(RouteChats)
//                LoginContract.Effect.Navigation.ToOAuth -> navController.navigate(RouteOAuthLogin)
//                LoginContract.Effect.Navigation.ToRegistration -> navController.navigate(RouteRegistration)
                else -> {}
            }
        }
    )
}


@Composable
fun LoginScreen(
    state: LoginContract.State,
    effectFlow: Flow<LoginContract.Effect>?,
    onEventSent: (LoginContract.Event) -> Unit,
    onNavigationRequested: (LoginContract.Effect.Navigation) -> Unit
) {
    val ctx = LocalContext.current
    LaunchedEffect(1) {
        effectFlow?.onEach { effect ->
            when (effect) {
//                is SplashContract.Effect.Navigation -> onNavigationRequested(effect)
                is LoginContract.Effect.Navigation -> onNavigationRequested(effect)
                is LoginContract.Effect.ShowError -> toast(effect.msg, ctx, ToastDuration.LONG)
            }
        }?.collect()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dp20),
        verticalArrangement = Arrangement.Center,
    ) {
        Row {
            Input(
                title = "Сервер",
                placeholder = "http://example.com",
                value = state.server,
                onValueChange = { onEventSent(LoginContract.Event.SetServer(it)) }
            )
            androidx.compose.material3.Button(
                modifier = Modifier
                    .background(
                        when (state.connStatus) {
                            LoginContract.ConnStatus.Err -> Pink
                            LoginContract.ConnStatus.Incompatible -> Copper
                            LoginContract.ConnStatus.None -> Olive
                            LoginContract.ConnStatus.Ok -> BlueSky
                        }
                    )
                    .size(20.dp),
                onClick = { onEventSent(LoginContract.Event.CheckConn) },
            )
            {}
        }
        Input(
            title = "Логин",
            placeholder = "Enter key for access to server",
            value = state.login,
            onValueChange = { onEventSent(LoginContract.Event.SetLogin(it)) }
        )
        Input(
            title = "Пароль",
            placeholder = "Enter key for access to server",
            value = state.password,
            onValueChange = { onEventSent(LoginContract.Event.SetPassword(it)) }
        )
        Button(
            onClick = { onEventSent(LoginContract.Event.Enter) },
            text = "Enter",
        )
        Button(
            onClick = { onEventSent(LoginContract.Event.GoToRegistration) },
            text = "Перейти к регистрации",
        )
        Button(
            onClick = { onEventSent(LoginContract.Event.GoToOAuth) },
            text = "Вход через сторонний сервис",
        )
    }

}

class LoginContract {
    sealed interface Event : ViewEvent {
        object CheckConn : Event
        object Enter : Event
        object GoToRegistration : Event
        object GoToOAuth : Event
        class SetServer(val value: String) : Event
        class SetLogin(val value: String) : Event
        class SetPassword(val value: String) : Event
    }

    data class State(
        val server: String = "",
        val connStatus: ConnStatus = ConnStatus.None,
        val login: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
    ) : ViewState

    sealed interface ConnStatus {
        object Ok : ConnStatus
        object Incompatible : ConnStatus
        object Err : ConnStatus
        object None : ConnStatus
    }

    sealed interface Effect : ViewSideEffect {
        data class ShowError(val msg: String) : Effect

        sealed interface Navigation : Effect {
            object ToOAuth : Navigation
            object ToRegistration : Navigation
            object ToHome : Navigation
        }
    }
}

sealed interface CheckConnResult {
    object None : CheckConnResult
    object Successful : CheckConnResult
    data class Err(val msg: String) : CheckConnResult
}


interface NPChatClient {
    fun ping(server: String): Result<Unit>
//    fun healthCheck(): Result<Health>
}

class LoginViewModel(
    private val repo: NPChatRepository,
    private val client: NPChatClient,
) : BaseViewModel<LoginContract.Event, LoginContract.State, LoginContract.Effect>() {

    private suspend fun checkConn() {
        client.ping(viewState.value.server)
            .onSuccess {
                setState { copy(connStatus = LoginContract.ConnStatus.Ok) }
            }
            .onFailure { res ->
                setState { copy(connStatus = LoginContract.ConnStatus.Err) }
                val err = res.message.orEmpty().ifEmpty { "emptyErr" }
                LoginContract.Effect.ShowError(err)
            }
    }

    private suspend fun enter() {
        if (viewState.value.connStatus == LoginContract.ConnStatus.None) {
            checkConn()
        }
        if (viewState.value.connStatus == LoginContract.ConnStatus.Err) {
            setEffect { LoginContract.Effect.ShowError("нет соединения с сервером") }
        }

        repo.login(
            login = viewState.value.login,
            password = viewState.value.password,
            server = viewState.value.server,
        ).onSuccess {
            setEffect { LoginContract.Effect.Navigation.ToHome }
        }.onFailure { res ->
            setEffect {
                val err = res.message.orEmpty().ifEmpty { "emptyErr" }
                LoginContract.Effect.ShowError(err)
            }
        }
    }

    override fun setInitialState() = LoginContract.State()

    override fun handleEvents(event: LoginContract.Event) {
        when (event) {
            LoginContract.Event.CheckConn -> viewModelScope.launch { checkConn() }
            LoginContract.Event.Enter -> viewModelScope.launch { enter() }
            LoginContract.Event.GoToOAuth -> setEffect { LoginContract.Effect.Navigation.ToOAuth }
            LoginContract.Event.GoToRegistration -> setEffect { LoginContract.Effect.Navigation.ToRegistration }
            is LoginContract.Event.SetLogin -> setState { copy(login = event.value) }
            is LoginContract.Event.SetPassword -> setState { copy(password = event.value) }
            is LoginContract.Event.SetServer -> setState { copy(server = event.value) }
        }
    }

}