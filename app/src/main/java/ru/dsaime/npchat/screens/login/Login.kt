package ru.dsaime.npchat.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.eventHandler
import ru.dsaime.npchat.common.functions.ToastDuration
import ru.dsaime.npchat.common.functions.toast
import ru.dsaime.npchat.ui.components.Gap
import ru.dsaime.npchat.ui.components.HostSelect
import ru.dsaime.npchat.ui.components.Input
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.RightButton
import ru.dsaime.npchat.ui.theme.Dp20
import ru.dsaime.npchat.ui.theme.Font

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
        state = vm.viewState.collectAsState().value,
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
        HostSelect(
            host = state.host,
            onClick = onEventSent.eventHandler(LoginEvent.SelectHost),
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
        RightButton(
            text = "Войти",
            onClick = onEventSent.eventHandler(LoginEvent.Enter),
        )
        LeftButton(
            text = "Перейти к регистрации",
            onClick = onEventSent.eventHandler(LoginEvent.GoToRegistration),
        )
        Gap(20.dp)
        Text("или", style = Font.Text16W400, modifier = Modifier.align(Alignment.CenterHorizontally))
        Gap(20.dp)
        LeftButton(
            text = "Вход через сторонний сервис",
            onClick = onEventSent.eventHandler(LoginEvent.GoToOAuth),
        )
    }
}
