package ru.dsaime.npchat.screens.control.profile.session.logout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.ui.components.Paragraph
import ru.dsaime.npchat.ui.components.RightButton
import ru.dsaime.npchat.ui.components.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.components.dialog.BottomDialogParams

object LogoutReq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutDialog(
    params: BottomDialogParams,
    vm: LogoutViewModel,
    onNavigationRequest: (LogoutEffect.Navigation) -> Unit,
) {
    val state = vm.viewState.collectAsState().value

    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is LogoutEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    BottomDialogHeader("Завершить сессию", params)
    Paragraph(
        "Чтобы снова войти в профиль, вам придется на" +
            "экране входа в приложение воспользоваться " +
            "одним из подключенных методов аутентификации.",
    )
    Paragraph("Вы действительно хотите выйти из своего профиля?")
    RightButton("Подтвердить", vm.eventHandler(LogoutEvent.Confirm))
}

sealed interface LogoutEvent {
    object Confirm : LogoutEvent

    object Back : LogoutEvent
}

object LogoutState

sealed interface LogoutEffect {
    sealed interface Navigation : LogoutEffect {
        object Login : Navigation

        object Back : Navigation
    }
}

class LogoutViewModel(
    private val sessionsService: SessionsService,
) : BaseViewModel<LogoutEvent, LogoutState, LogoutEffect>() {
    override fun setInitialState() = LogoutState

    override fun handleEvents(event: LogoutEvent) {
        when (event) {
            LogoutEvent.Back -> LogoutEffect.Navigation.Back.emit()
            LogoutEvent.Confirm ->
                viewModelScope.launch {
                    val session = sessionsService.currentSession() ?: return@launch
                    sessionsService.revoke(session)
                    LogoutEffect.Navigation.Login.emit()
                }
        }
    }
}
