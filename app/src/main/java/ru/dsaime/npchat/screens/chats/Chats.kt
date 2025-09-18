package ru.dsaime.npchat.screens.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.BaseViewModel
import ru.dsaime.npchat.data.ChatsService
import ru.dsaime.npchat.model.Chat
import ru.dsaime.npchat.screens.chats.Effect.Navigation.ToChat
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
fun ChatsScreenDestination(onNavigationRequest: (Effect.Navigation) -> Unit) {
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
    state: State,
    effectFlow: Flow<Effect>,
    onEventSent: (Event) -> Unit,
    onNavigationRequest: (Effect.Navigation) -> Unit,
) {
    LaunchedEffect(1) {
        effectFlow
            .onEach { effect ->
                when (effect) {
                    is Effect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }

    val lazyListState = rememberLazyListState()

    val lastVisibleIndex by
        remember {
            derivedStateOf {
                lazyListState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index
            }
        }
    LaunchedEffect(lastVisibleIndex) {
        onEventSent(Event.LastVisibleIndexChanged(lastVisibleIndex))
    }

//    var isPageRequesterReached by remember { mutableStateOf(false) }
//    LaunchedEffect(isPageRequesterReached) {
//        if (isPageRequesterReached) {
//            onEventSent(Event.LoadNextItems)
//        }
//    }

//    val isPageRequesterVisible by
//        remember {
//            derivedStateOf { state.items.contains(ChatsItem.PageRequester) }
//        }
//    LaunchedEffect(isPageRequesterVisible) {
//        if (!isPageRequesterVisible) {
//            isPageRequesterReached = false
//        }
//    }

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
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(state.chats) { chat ->
                ChatCard(
                    chat = chat,
                    lastMessage = state.lastMessage[chat.id],
                    unreadCount = state.unread[chat.id] ?: 0,
                    onClick = { onEventSent(Event.SelectChat(chat)) },
                )
            }

            item {
                when (state.trailing) {
                    Trailing.Loading -> CircularProgressIndicator()
                    is Trailing.Err ->
                        Button("RetryLoading (err:${state.trailing.msg})", onClick = {
                            onEventSent(Event.RetryPage)
                        })

                    null -> Text("Конец", style = Font.Text14W400)
                }
            }
        }
    }
}

sealed interface Event {
    class SelectChat(
        val chat: Chat,
    ) : Event

    class LastVisibleIndexChanged(
        val value: Int?,
    ) : Event

    //    object LoadNextItems : Event
//
    object RetryPage : Event

//    object Reload : ChatsEvent
//
//    object LoadNext : ChatsEvent
}

// sealed interface ChatsItem {
//    data class Chat(
//        val value: ru.dsaime.npchat.model.Chat,
//    ) : ChatsItem
//
// //    object PageRequester : ChatsItem
//
//    class FailedRequest(
//        val msg: String,
//    ) : ChatsItem
//
//    object Loading : ChatsItem
// }

sealed interface Trailing {
    class Err(
        var msg: String,
    ) : Trailing

    object Loading : Trailing
}

sealed interface Content {
    class Err(
        val msg: String,
    ) : Content

    object Loading : Content

    object Ok : Content
}

data class State(
    val content: Content = Content.Loading,
    val chats: List<Chat> = emptyList(),
    val trailing: Trailing? = null,
    val lastMessage: Map<String, MessageUI> = emptyMap(),
    val unread: Map<String, Int> = emptyMap(),
)

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

sealed interface Effect {
    sealed interface Navigation : Effect {
        class ToChat(
            val chat: Chat,
        ) : Navigation
    }
}

class ChatsViewModel(
    private val chatsService: ChatsService,
) : BaseViewModel<Event, State, Effect>() {
    override fun setInitialState() = State()

    private var keysetForNext = ""
    private var pagingFinished = false
    private val itemsBeforeLoading = 10

    init {

        viewModelScope.launch {
            loadNextPage()
        }
    }

    private suspend fun loadNextPage() {
        setState { copy(trailing = Trailing.Loading) }
        chatsService
            .myChats(keysetForNext)
            .onSuccess {
                keysetForNext = it.nextKeyset
                pagingFinished = it.chats.isEmpty()
                setState { copy(chats = chats + it.chats, trailing = null) }
            }.onFailure {
                setState { copy(trailing = Trailing.Err(it)) }
            }
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.SelectChat -> ToChat(event.chat).emit()
            Event.RetryPage -> viewModelScope.launch { loadNextPage() }
            is Event.LastVisibleIndexChanged -> {
                if (event.value == null) {
                    return
                }
                if (pagingFinished) {
                    return
                }
                if (viewState.value.trailing is Trailing.Loading) {
                    return
                }
                if (viewState.value.chats.lastIndex - event.value < itemsBeforeLoading) {
                    viewModelScope.launch {
                        loadNextPage()
                    }
                }
            }
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
