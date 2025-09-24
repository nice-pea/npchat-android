package ru.dsaime.npchat.screens.hosts.select

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.model.Host
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.dialog.BottomDialogHeader

object HostSelectReq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostSelectDialogContent(onNavigationRequest: (HostSelectEffect.Navigation) -> Unit) {
    val vm = koinViewModel<HostSelectViewModel>()
    val state = vm.viewState.value

    val ctx = LocalContext.current
    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is HostSelectEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    BottomDialogHeader("Управление")
    LeftButton("Создать чат", vm.eventHandler(HostSelectEvent.CreateChat))
    LeftButton("Профиль", vm.eventHandler(HostSelectEvent.Profile))
    LeftButton("Выйти", vm.eventHandler(HostSelectEvent.ProfileExit), isRight = true)
}

sealed interface HostSelectEvent {
    object Add : HostSelectEvent

    class Delete : HostSelectEvent

    class Select(
        val host: Host,
    ) : HostSelectEvent
}

data class HostSelectState(
    val hosts: List<Host> = emptyList(),
    val selectedHost: Host? = null,
)

sealed interface HostSelectEffect {
    sealed interface Navigation : HostSelectEffect {
        object Close : Navigation
        class Add
    }
}

class HostSelectViewModel(
    private val hostService: HostService,
) : BaseViewModel<HostSelectEvent, HostSelectState, HostSelectEffect>() {
    override fun setInitialState() = HostSelectState()

    override fun handleEvents(event: HostSelectEvent) {
        when (event) {
            HostSelectEvent.Add ->
            is HostSelectEvent.Delete -> TODO()
            is HostSelectEvent.Select -> TODO()
        }
    }
}
