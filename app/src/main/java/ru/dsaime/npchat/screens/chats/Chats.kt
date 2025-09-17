package ru.dsaime.npchat.screens.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.ChatsService
import ru.dsaime.npchat.model.Chat
import ru.dsaime.npchat.screens.chats.ChatsEffect.Navigation.ToChat
import ru.dsaime.npchat.ui.components.Button
import ru.dsaime.npchat.ui.components.Gap
import ru.dsaime.npchat.ui.theme.ColorBG
import ru.dsaime.npchat.ui.theme.ColorText
import ru.dsaime.npchat.ui.theme.Dp20
import ru.dsaime.npchat.ui.theme.Dp4
import ru.dsaime.npchat.ui.theme.Dp8
import ru.dsaime.npchat.ui.theme.Font
import ru.dsaime.npchat.ui.theme.RoundMin
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChatsScreenDestination(onNavigationRequest: (ChatsEffect.Navigation) -> Unit) {
    val vm = koinViewModel<ChatsViewModel>()
    ChatsScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::handleEvents,
        onNavigationRequest = onNavigationRequest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    state: ChatsState,
    effectFlow: Flow<ChatsEffect>,
    onEventSent: (ChatsEvent) -> Unit,
    onNavigationRequest: (ChatsEffect.Navigation) -> Unit,
) {
    LaunchedEffect(1) {
        effectFlow
            .onEach { effect ->
                when (effect) {
                    is ChatsEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    var isLoadRequesterReached by remember { mutableStateOf(false) }
    LaunchedEffect(isLoadRequesterReached) {
        if (isLoadRequesterReached) {
            onEventSent(ChatsEvent.LoadNextItems)
        }
    }

    val isLoadRequesterVisible by
        remember {
            derivedStateOf { state.items.contains(ChatsItem.LoadRequester) }
        }
    LaunchedEffect(isLoadRequesterVisible) {
        if (!isLoadRequesterVisible) {
            isLoadRequesterReached = false
        }
    }

    Scaffold(
        containerColor = ColorBG,
        topBar = {
            TopAppBar(
                title = { Text("Все чаты") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = ColorBG,
                        titleContentColor = ColorText,
                    ),
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            state.items.forEach { it ->
                when (it) {
                    ChatsItem.LoadRequester -> isLoadRequesterReached = true
                    ChatsItem.Loading -> CircularProgressIndicator()
                    is ChatsItem.FailedRequest ->
                        Button("RetryLoading", onClick = {
                            onEventSent(ChatsEvent.RetryLoadNextItems)
                        })

                    is ChatsItem.Chat ->
                        ChatCard(
                            chat = it.value,
                            lastMessage = state.lastMessage[it.value.id],
                            unreadCount = state.unread[it.value.id] ?: 0,
                            onClick = { onEventSent(ChatsEvent.SelectChat(it.value)) },
                        )
                }
            }
        }
    }
}

sealed interface ChatsEvent {
    class SelectChat(
        val chat: Chat,
    ) : ChatsEvent

    object LoadNextItems : ChatsEvent

    object RetryLoadNextItems : ChatsEvent

//    object Reload : ChatsEvent
//
//    object LoadNext : ChatsEvent
}

sealed interface ChatsItem {
    data class Chat(
        val value: ru.dsaime.npchat.model.Chat,
    ) : ChatsItem

    object LoadRequester : ChatsItem

    class FailedRequest(
        val msg: String,
    ) : ChatsItem

    object Loading : ChatsItem
}

sealed interface ChatsContent {
    class Err(
        val msg: String,
    ) : ChatsContent

    object Loading : ChatsContent

    object Ok : ChatsContent
}

data class ChatsState(
    val content: ChatsContent = ChatsContent.Loading,
    val items: List<ChatsItem> = emptyList(),
    val lastMessage: Map<String, MessageUI> = emptyMap(),
    val unread: Map<String, Int> = emptyMap(),
)

// sealed interface MessageUI {
//    data class Text(
//        val author: String,
//        val text: String,
//        val date: OffsetDateTime,
//    ) : MessageUI
//
//    data class Deleted(
//        val author: String,
//        val date: OffsetDateTime,
//    ) : MessageUI
//
//    data class Action(
//        val author: String,
//        val date: OffsetDateTime,
//    ) : MessageUI
// }

data class MessageUI(
    val author: String,
    val text: String,
    val date: OffsetDateTime,
    val type: Type,
) {
    enum class Type {
        Text,
        Deleted,
        Action,
    }
}

sealed interface ChatsEffect {
    sealed interface Navigation : ChatsEffect {
        class ToChat(
            val chat: Chat,
        ) : Navigation
    }
}

class ChatsViewModel(
    private val chatsService: ChatsService,
) : BaseViewModel<ChatsEvent, ChatsState, ChatsEffect>() {
    override fun setInitialState() = ChatsState()

    init {
        chatsService.myChats()
    }

    override fun handleEvents(event: ChatsEvent) {
        when (event) {
            is ChatsEvent.SelectChat -> ToChat(event.chat).emit()
            ChatsEvent.LoadNextItems -> TODO()
            ChatsEvent.RetryLoadNextItems -> TODO()
        }
    }
}

@Preview(
    backgroundColor = 0xFF000000,
    showBackground = true,
)
@Composable
private fun PreviewChatCard() {
    ChatCard(
        onClick = {},
        lastMessage = null,
        unreadCount = 0,
        chat =
            Chat(
                id = "adf",
                name = "ChatName",
                chiefId = "fg",
            ),
    )
}

@Composable
private fun ChatCard(
    chat: Chat,
    lastMessage: MessageUI?,
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundMin)
                .clickable(onClick = onClick)
                .padding(horizontal = Dp20, vertical = Dp8)
                .then(modifier),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = chat.name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                style = Font.White14W400,
            )
            if (unreadCount > 0) {
                Gap(Dp4)
                Text("+$unreadCount", style = Font.White12W500)
            }
        }
        if (lastMessage == null) {
            Text(
                text = "Нет сообщений",
                modifier = Modifier.padding(start = Dp8),
                style = Font.Deleted14W500,
            )
        } else {
            LastMessage(lastMessage, modifier = Modifier.padding(start = Dp8))
        }
    }
}

@Composable
fun LastMessage(
    msg: MessageUI,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            Modifier
                .padding(start = Dp8)
                .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (msg.author.isNotBlank()) {
            Text(msg.author, maxLines = 1, style = Font.Sender12W400)
            Gap(Dp4)
        }
        Text(
            text = msg.text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            style =
                when (msg.type) {
                    MessageUI.Type.Text -> Font.Text14W400
                    MessageUI.Type.Deleted -> Font.Deleted14W400
                    MessageUI.Type.Action -> Font.Action14W400
                },
        )
        Gap(Dp4)
        Text(msg.date.format(msgDateFormat), style = Font.Text12W400)
    }
}

private val msgDateFormat = DateTimeFormatter.ofPattern("HH:mm")
