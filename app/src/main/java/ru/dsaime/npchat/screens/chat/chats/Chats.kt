package ru.dsaime.npchat.screens.chat.chats

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import ru.dsaime.npchat.common.functions.toast
import ru.dsaime.npchat.data.ChatsService
import ru.dsaime.npchat.data.EventsService
import ru.dsaime.npchat.model.Chat
import ru.dsaime.npchat.ui.components.Gap
import ru.dsaime.npchat.ui.components.LeftButton
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
        state = vm.viewState.collectAsState().value,
        effectFlow = vm.effect,
        onEventSent = vm::setEvent,
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
    val ctx = LocalContext.current
    LaunchedEffect(1) {
        effectFlow
            .onEach { effect ->
                when (effect) {
                    is ChatsEffect.Navigation -> onNavigationRequest(effect)
                    is ChatsEffect.Err -> toast(effect.msg, ctx)
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
        onEventSent(ChatsEvent.LastVisibleIndexChanged(lastVisibleIndex))
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
                    onClick = { onEventSent(ChatsEvent.SelectChat(chat)) },
                )
            }

            item {
                when (state.trailing) {
                    Trailing.Loading -> CircularProgressIndicator()
                    is Trailing.Err ->
                        LeftButton("RetryLoading (err:${state.trailing.msg})", onClick = {
                            onEventSent(ChatsEvent.RetryPage)
                        })

                    null -> Text("Конец", style = Font.Text14W400)
                }
            }
        }
    }
}

sealed interface ChatsEvent {
    class SelectChat(
        val chat: Chat,
    ) : ChatsEvent

    class LastVisibleIndexChanged(
        val value: Int?,
    ) : ChatsEvent

    //    object LoadNextItems : Event
//
    object RetryPage : ChatsEvent

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

data class ChatsState(
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

sealed interface ChatsEffect {
    class Err(
        val msg: String,
    ) : ChatsEffect

    sealed interface Navigation : ChatsEffect {
        class Chat(
            val chat: ru.dsaime.npchat.model.Chat,
        ) : Navigation
    }
}

class ChatsViewModel(
    private val chatsService: ChatsService,
    private val eventsService: EventsService,
) : BaseViewModel<ChatsEvent, ChatsState, ChatsEffect>() {
    override fun setInitialState() = ChatsState()

    private var pageTokenForNext = ""
    private var pagingFinished = false
    private val itemsBeforeLoading = 10

    init {
        viewModelScope.launch {
            loadNextPage()
            eventsService
                .onChatCreated()
                .collect { result ->
                    result
                        .onSuccess { event ->
                            setState { copy(chats = listOf(event.chat) + chats) }
                        }.onFailure {
                            ChatsEffect.Err(it).emit()
                        }
                }
        }
    }

    private suspend fun loadNextPage() {
        setState { copy(trailing = Trailing.Loading) }
        chatsService
            .myChats(pageTokenForNext)
            .onSuccess {
                pageTokenForNext = it.nextPageToken
                pagingFinished = it.nextPageToken.isBlank()
                setState { copy(chats = chats + it.chats, trailing = null) }
            }.onFailure {
                setState { copy(trailing = Trailing.Err(it)) }
            }
    }

    override fun handleEvents(event: ChatsEvent) {
        when (event) {
            is ChatsEvent.SelectChat -> ChatsEffect.Navigation.Chat(event.chat).emit()
            ChatsEvent.RetryPage -> viewModelScope.launch { loadNextPage() }
            is ChatsEvent.LastVisibleIndexChanged -> {
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
