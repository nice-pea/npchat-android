package ru.dsaime.npchat.screens.chat.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.common.functions.ToastDuration
import ru.dsaime.npchat.common.functions.toast
import ru.dsaime.npchat.data.ChatsService
import ru.dsaime.npchat.ui.components.Input
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.dialog.BottomDialogHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatDialogContent(
    showBackButton: Boolean,
    onNavigationRequest: (CreateChatEffect.Navigation) -> Unit,
) {
    val vm = koinViewModel<CreateChatViewModel>()
    val state by vm.viewState.collectAsState()

    val ctx = LocalContext.current
    LaunchedEffect(1) {
        vm.effect
            .onEach { effect ->
                when (effect) {
                    is CreateChatEffect.Navigation -> onNavigationRequest(effect)
                    is CreateChatEffect.ShowToast -> toast(effect.msg, ctx, ToastDuration.LONG)
                }
            }.collect()
    }

    BottomDialogHeader(
        "Создать чат",
        onBack =
            vm
                .eventHandler(CreateChatEvent.Back)
                .takeIf { showBackButton },
    )
    Input(
        value = state.name,
        title = "Название",
        placeholder = "Введите название",
        onValueChange = vm.eventHandler(CreateChatEvent::SetName),
        helperText = "Позже можно изменить",
    )
    LeftButton("Создать", vm.eventHandler(CreateChatEvent.Confirm), isRight = true)
}

sealed interface CreateChatEvent {
    class SetName(
        val value: String,
    ) : CreateChatEvent

    object Confirm : CreateChatEvent

    object Back : CreateChatEvent
}

data class CreateChatState(
    val name: String = "",
)

sealed interface CreateChatEffect {
    data class ShowToast(
        val msg: String,
    ) : CreateChatEffect

    sealed interface Navigation : CreateChatEffect {
        class Chat(
            val chat: ru.dsaime.npchat.model.Chat,
        ) : Navigation

        object Back : Navigation

        object Close : Navigation
    }
}

class CreateChatViewModel(
    private val chatsService: ChatsService,
) : BaseViewModel<CreateChatEvent, CreateChatState, CreateChatEffect>() {
    override fun setInitialState() = CreateChatState()

    override fun handleEvents(event: CreateChatEvent) {
        when (event) {
            is CreateChatEvent.SetName -> setState { copy(name = event.value) }
            CreateChatEvent.Confirm ->
                viewModelScope.launch {
                    chatsService
                        .create(viewState.value.name)
                        .onSuccess {
                            CreateChatEffect.ShowToast("Чат создан").emit()
                            CreateChatEffect.Navigation.Chat(it).emit()
                            CreateChatEffect.Navigation.Close.emit()
                        }.onFailure {
                            CreateChatEffect.ShowToast(it).emit()
                        }
                }

            CreateChatEvent.Back -> CreateChatEffect.Navigation.Back.emit()
        }
    }
}
