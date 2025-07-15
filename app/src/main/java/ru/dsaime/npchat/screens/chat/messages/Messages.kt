package ru.dsaime.npchat.screens.chat.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.dsaime.npchat.common.AsyncData
import ru.dsaime.npchat.common.asyncDataErr
import ru.dsaime.npchat.common.asyncDataMutFlow
import ru.dsaime.npchat.common.asyncDataOk
import ru.dsaime.npchat.data.repositories.Boundary
import ru.dsaime.npchat.data.repositories.ChatsRepository
import ru.dsaime.npchat.data.repositories.MessagesRepository
import ru.dsaime.npchat.data.store.AuthenticationStore
import ru.dsaime.npchat.data.store.Profile
import ru.dsaime.npchat.model.Model
import ru.dsaime.npchat.ui.theme.Font
import ru.dsaime.npchat.ui.theme.White

data class RouteMessages(
    val chatID: Int,
)

@Composable
fun MessagesScreen(
    navController: NavController,
    chatID: Int,
) {
    val vm = koinViewModel<MessagesViewModel> { parametersOf(chatID) }
    val chat = vm.uiState.chat.collectAsState().value
    Column {
        AppBar(
            appBarTitle = when (chat) {
                is AsyncData.Err -> chat.err.message.orEmpty()
                AsyncData.None -> "init"
                AsyncData.Loading -> "Loading...."
                is AsyncData.Ok -> chat.data.name
            },
            clickMenu = TODO(),
        )

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    appBarTitle: String,
    clickMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(appBarTitle, style = Font.White20W400) },
        actions = {
            IconButton(onClick = clickMenu) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    tint = White,
                    contentDescription = "menu"
                )
            }
        }
    )
}

data class MessagesUiState(
    val chat: StateFlow<AsyncData<Model.Chat>>,
    val messages: StateFlow<AsyncData<List<Model.Message>>>,
    val profile: StateFlow<AsyncData<Profile>>,
)

class MessagesViewModel(
    private val chatID: Int,
    private val chatsRepo: ChatsRepository,
    private val messagesRepo: MessagesRepository,
    private val authnStore: AuthenticationStore,
) : ViewModel() {
    private val chat = asyncDataMutFlow<Model.Chat>()
    private val messages = asyncDataMutFlow<List<Model.Message>>()
    private val profile = asyncDataMutFlow<Profile>()

    val uiState = MessagesUiState(
        chat = chat,
        messages = messages,
        profile = profile,
    )

    init {
        loadProfile()
        viewModelScope.launch {
            loadChat()
            loadMessages()
        }
    }

    private fun loadProfile() {
        authnStore.profile()
            .onSuccess { profile.value = it.asyncDataOk() }
            .onFailure { profile.value = it.asyncDataErr() }
    }

    private suspend fun loadChat() {
        chatsRepo.chat(chatID)
            .onSuccess { chat.value = it.asyncDataOk() }
            .onFailure { chat.value = it.asyncDataErr() }
    }

    private suspend fun loadMessages(id: Int? = null) {
        messagesRepo.messages(
            chatID = chatID,
            id = id,
            boundary = Boundary.Before,
            limit = 33,
        )
            .onSuccess { messages.value = it.asyncDataOk() }
            .onFailure { messages.value = it.asyncDataErr() }
    }

}