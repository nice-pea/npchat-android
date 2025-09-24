package ru.dsaime.npchat.screens.hosts.select

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
import ru.dsaime.npchat.common.functions.tickerFlow
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.model.Host
import ru.dsaime.npchat.ui.components.Gap
import ru.dsaime.npchat.ui.components.HostStatusIcon
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.RadioButton
import ru.dsaime.npchat.ui.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.theme.Dp16
import ru.dsaime.npchat.ui.theme.Font
import kotlin.time.Duration.Companion.seconds

object HostSelectReq

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.HostSelectDialogContent(onNavigationRequest: (HostSelectEffect.Navigation) -> Unit) {
    val vm = koinViewModel<HostSelectViewModel>()
    val state = vm.viewState.value
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
    }
}

class HostSelectViewModel(
    private val hostService: HostService,
) : BaseViewModel<HostSelectEvent, HostSelectState, HostSelectEffect>() {
    override fun setInitialState() = HostSelectState()

    init {
        viewModelScope.launch {
            val hosts =
                hostService
                    .savedBaseUrls()
                    .map { Host(it, Host.Status.UNKNOWN) }
            setState { copy(hosts = hosts) }
            launch { subscribeToHostChanges() }
            subscribeToHostStatusChanges()
        }
    }

    private suspend fun subscribeToHostChanges() {
        hostService.currentBaseUrlFlow().collectLatest { currentHost ->
            // Обновлять выбранный хост
            val host = if (currentHost != null) Host(currentHost, Host.Status.UNKNOWN) else null
            setState { copy(selectedHost = host) }
        }
    }

    private suspend fun subscribeToHostStatusChanges() {
        tickerFlow(1.seconds)
            .onEach { println("tick") }
            .collect {
                viewState.value.hosts.forEach { host ->
                    // Получить новый статус хоста
                    val newStatus = hostService.status(host.url)
                    if (newStatus == host.status) return@forEach
                    // Обновить статус хоста в списке
                    val newHosts =
                        viewState.value.hosts
                            .map { if (it.url == host.url) it.copy(status = newStatus) else it }
                    // Обновить список
                    setState { copy(hosts = newHosts) }
                }
            }
    }

    override fun handleEvents(event: HostSelectEvent) {
        when (event) {
            HostSelectEvent.Add -> {}
            HostSelectEvent.Delete ->
                viewModelScope.launch {
                    val baseUrl = viewState.value.selectedHost?.url ?: return@launch
                    hostService.deleteBaseUrl(baseUrl)
                }

            is HostSelectEvent.Select -> {
                viewModelScope.launch {
                    hostService.changeBaseUrl(event.host.url)
                }
            }
        }
    }
}
