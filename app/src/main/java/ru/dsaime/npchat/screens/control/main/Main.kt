package ru.dsaime.npchat.screens.control.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.dialog.BottomDialogHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlDialogContent(
    vm: ControlViewModel,
    onNavigationRequest: (ControlEffect.Navigation) -> Unit,
) {
    val state = vm.viewState.collectAsState().value

    val ctx = LocalContext.current
    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is ControlEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    BottomDialogHeader("Управление")
    LeftButton("Создать чат", vm.eventHandler(ControlEvent.CreateChat))
    LeftButton("Профиль", vm.eventHandler(ControlEvent.Profile))
}

sealed interface ControlEvent {
    object CreateChat : ControlEvent

    object Profile : ControlEvent
}

object ControlState

sealed interface ControlEffect {
    sealed interface Navigation : ControlEffect {
        object CreateChat : Navigation

        object Profile : Navigation
    }
}

class ControlViewModel(
    private val sessionsService: SessionsService,
    private val hostService: HostService,
) : BaseViewModel<ControlEvent, ControlState, ControlEffect>() {
    override fun setInitialState() = ControlState

    override fun handleEvents(event: ControlEvent) {
        when (event) {
            ControlEvent.CreateChat -> ControlEffect.Navigation.CreateChat.emit()
            ControlEvent.Profile -> ControlEffect.Navigation.Profile.emit()
        }
    }
}
