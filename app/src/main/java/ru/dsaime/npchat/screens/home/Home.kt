package ru.dsaime.npchat.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import ru.dsaime.npchat.common.base.eventHandler
import ru.dsaime.npchat.ui.modifiers.topBorder
import ru.dsaime.npchat.ui.theme.ColorBG
import ru.dsaime.npchat.ui.theme.ColorPart

@Composable
fun HomeScreenDestination(
    onNavigationRequest: (HomeEffect.Navigation) -> Unit,
    content: @Composable () -> Unit,
) {
    val vm = koinViewModel<HomeViewModel>()
    HomeScreen(
        state = vm.viewState.value,
        effectFlow = vm.effect,
        onEventSent = vm::setEvent,
        onNavigationRequest = onNavigationRequest,
        content = content,
    )
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        state = HomeState,
        effectFlow = flow { },
        onEventSent = {},
        onNavigationRequest = {},
    ) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    effectFlow: Flow<HomeEffect>,
    onEventSent: (HomeEvent) -> Unit,
    onNavigationRequest: (HomeEffect.Navigation) -> Unit,
    content: @Composable () -> Unit,
) {
    val ctx = LocalContext.current
    LaunchedEffect(1) {
        effectFlow
            .onEach { effect ->
                when (effect) {
                    is HomeEffect.Navigation -> onNavigationRequest(effect)
                }
            }.collect()
    }
    Scaffold(
        containerColor = ColorBG,
        bottomBar = {
            val items =
                listOf(
                    BottomNavigationItem("Уведомления", Icons.Filled.Notifications, HomeEvent.NavChats),
                    BottomNavigationItem("Чат", Icons.Filled.Email, HomeEvent.NavChats),
                    BottomNavigationItem("Настройки", Icons.Filled.Settings, HomeEvent.NavControl),
                )
            NavigationBar(
                modifier =
                    Modifier
                        .height(50.dp)
                        .topBorder(ColorPart, 1f),
                containerColor = ColorBG,
                contentColor = ColorPart,
            ) {
                items.forEach {
                    NavigationBarItem(
                        selected = false,
                        onClick = onEventSent.eventHandler(it.event),
                        icon = { Icon(it.icon, contentDescription = it.title) },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = ColorPart,
                            ),
                    )
                }
            }
        },
    ) {
        Box(modifier = Modifier.padding(it)) {
            content()
        }
    }
}

data class BottomNavigationItem(
    val title: String,
    val icon: ImageVector,
    val event: HomeEvent,
)
