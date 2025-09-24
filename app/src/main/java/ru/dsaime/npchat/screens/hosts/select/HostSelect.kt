package ru.dsaime.npchat.screens.hosts.select

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.model.Host
import ru.dsaime.npchat.ui.components.Gap
import ru.dsaime.npchat.ui.components.HostStatusIcon
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.RadioButton
import ru.dsaime.npchat.ui.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.theme.Dp16
import ru.dsaime.npchat.ui.theme.Font

object HostSelectReq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.HostSelectDialogContent(onNavigationRequest: (HostSelectEffect.Navigation) -> Unit) {
    val vm = koinViewModel<HostSelectViewModel>()
    val state by vm.viewState.collectAsState()
    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is HostSelectEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    BottomDialogHeader("Выбрать сервер")
    state.hosts.forEach { host ->
        RadioButton(
            text = host.url,
            onClick = vm.eventHandler(HostSelectEvent.Select(host)),
            selected = host.url == state.selectedHost?.url,
            icon = { HostStatusIcon(host.status) },
        )
    }
    if (state.hosts.isEmpty()) {
        Text("Нет доступных серверов", style = Font.Text16W400)
    }
    Gap(Dp16)
    LeftButton("Добавить", vm.eventHandler(HostSelectEvent.Add))
    if (state.selectedHost != null) {
        LeftButton("Удалить выбранный", vm.eventHandler(HostSelectEvent.Delete), isRight = true)
    }
}

sealed interface HostSelectEvent {
    object Add : HostSelectEvent

    object Delete : HostSelectEvent

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

        object AddHost : Navigation
    }
}

class HostSelectViewModel(
    private val hostService: HostService,
) : BaseViewModel<HostSelectEvent, HostSelectState, HostSelectEffect>() {
    override fun setInitialState() = HostSelectState()

    init {
        viewModelScope.launch {
            launch { subscribeToCurrentHostChanges() }
            subscribeToHostChanges()
        }
    }

    // Обновлять выбранный хост
    private suspend fun subscribeToCurrentHostChanges() =
        hostService.currentHostFlow().collectLatest { host ->
            setState { copy(selectedHost = host) }
        }

    // Обновлять список хостов
    private suspend fun subscribeToHostChanges() =
        hostService.hostsFlow().collectLatest { hosts ->
            setState { copy(hosts = hosts.sortedBy { it.url }) }
        }

    override fun handleEvents(event: HostSelectEvent) {
        when (event) {
            HostSelectEvent.Add -> HostSelectEffect.Navigation.AddHost.emit()
            HostSelectEvent.Delete ->
                viewModelScope.launch {
                    val baseUrl = viewState.value.selectedHost?.url ?: return@launch
                    hostService.deleteHostByUrl(baseUrl)
                }

            is HostSelectEvent.Select ->
                viewModelScope.launch {
                    hostService.changeHost(event.host)
                }
        }
    }
}
