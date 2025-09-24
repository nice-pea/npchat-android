package ru.dsaime.npchat.screens.hosts.add

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

object AddHostReq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.AddHostDialogContent(onNavigationRequest: (AddHostEffect.Navigation) -> Unit) {
    val vm = koinViewModel<AddHostViewModel>()
    val state = vm.viewState.value
    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is AddHostEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    BottomDialogHeader("Выбрать сервер")
    state.hosts.forEach { host ->
        RadioButton(
            text = host.url,
            onClick = vm.eventHandler(AddHostEvent.Select(host)),
            selected = host.url == state.selectedHost?.url,
            icon = { HostStatusIcon(host.status) },
        )
    }
    if (state.hosts.isEmpty()) {
        Text("Нет доступных серверов", style = Font.Text16W400)
    }
    Gap(Dp16)
    LeftButton("Добавить", vm.eventHandler(AddHostEvent.Add))
    if (state.selectedHost != null) {
        LeftButton("Удалить выбранный", vm.eventHandler(AddHostEvent.Delete), isRight = true)
    }
}

sealed interface AddHostEvent {
    object Add : AddHostEvent

    object Delete : AddHostEvent

    class Select(
        val host: Host,
    ) : AddHostEvent
}

data class AddHostState(
    val hosts: List<Host> = emptyList(),
    val selectedHost: Host? = null,
)

sealed interface AddHostEffect {
    sealed interface Navigation : AddHostEffect {
        object Close : Navigation

        object Back : Navigation
    }
}

class AddHostViewModel(
    private val hostService: HostService,
) : BaseViewModel<AddHostEvent, AddHostState, AddHostEffect>() {
    override fun setInitialState() = AddHostState()

    init {
        viewModelScope.launch {
            launch { subscribeToCurrentHostChanges() }
            subscribeToHostChanges()
        }
    }

    // Обновлять выбранный хост
    private suspend fun subscribeToCurrentHostChanges() {
        hostService.currentHostFlow().collectLatest { host ->
            setState { copy(selectedHost = host) }
        }
    }

    // Обновлять список хостов
    private suspend fun subscribeToHostChanges() {
        hostService.hostsFlow().collectLatest { hosts ->
            setState { copy(hosts = hosts) }
        }
    }

    override fun handleEvents(event: AddHostEvent) {
        when (event) {
            AddHostEvent.Add -> {}
            AddHostEvent.Delete ->
                viewModelScope.launch {
                    val baseUrl = viewState.value.selectedHost?.url ?: return@launch
                    hostService.deleteHostByUrl(baseUrl)
                }

            is AddHostEvent.Select -> {
                viewModelScope.launch {
                    hostService.changeHost(event.host)
                }
            }
        }
    }
}
