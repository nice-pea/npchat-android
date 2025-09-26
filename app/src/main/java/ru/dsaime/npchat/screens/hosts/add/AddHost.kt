package ru.dsaime.npchat.screens.hosts.add

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.model.Host
import ru.dsaime.npchat.ui.components.HostStatusIcon
import ru.dsaime.npchat.ui.components.Input
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.components.dialog.BottomDialogParams

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHostDialogContent(
    params: BottomDialogParams,
    onNavigationRequest: (AddHostEffect.Navigation) -> Unit,
) {
    val vm = koinViewModel<AddHostViewModel>()
    val state by vm.viewState.collectAsState()
    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is AddHostEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    BottomDialogHeader("Добавить сервер", params)
    Input(
        title = "Адрес",
        value = state.url,
        onValueChange = vm.eventHandler(AddHostEvent::SetUrl),
        placeholder = "http://192.168.1.1:8080",
    )
    HostStatusIcon(state.status)
    LeftButton("Добавить", vm.eventHandler(AddHostEvent.Confirm), isRight = true)
}

sealed interface AddHostEvent {
    object Confirm : AddHostEvent

    object Back : AddHostEvent

    data class SetUrl(
        val value: String,
    ) : AddHostEvent
}

data class AddHostState(
    val url: String = "",
    val status: Host.Status = Host.Status.UNKNOWN,
)

sealed interface AddHostEffect {
    sealed interface Navigation : AddHostEffect {
        object Close : Navigation

        object Back : Navigation
    }
}

@OptIn(FlowPreview::class)
class AddHostViewModel(
    private val hostService: HostService,
) : BaseViewModel<AddHostEvent, AddHostState, AddHostEffect>() {
    override fun setInitialState() = AddHostState()

    init {
        viewModelScope.launch {
            viewState
                .debounce(300L) // Wait 300ms after the last input
                .map { it.url }
                .distinctUntilChanged() // Optional: only process if input has changed
                .filter { it.isNotBlank() }
                .collect { url ->
                    val newStatus = hostService.status(url)
                    setState { copy(status = newStatus) }
                }
        }
    }

    override fun handleEvents(event: AddHostEvent) {
        when (event) {
            is AddHostEvent.SetUrl -> setState { copy(url = event.value) }
            AddHostEvent.Back -> AddHostEffect.Navigation.Back.emit()
            AddHostEvent.Confirm ->
                viewModelScope.launch {
                    hostService.add(Host(url = viewState.value.url, status = viewState.value.status))
                    AddHostEffect.Navigation.Close.emit()
                }
        }
    }
}
