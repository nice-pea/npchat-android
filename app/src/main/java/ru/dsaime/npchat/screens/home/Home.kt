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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.androidx.compose.koinViewModel
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
        onEventSent = vm::handleEvents,
        onNavigationRequest = onNavigationRequest,
        content = content,
    )
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        state = HomeState(),
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
    Scaffold(
        containerColor = ColorBG,
        bottomBar = {
            val items =
                listOf(
                    BottomNavigationItem(title = "Уведомления", icon = Icons.Filled.Notifications),
                    BottomNavigationItem(title = "Чат", icon = Icons.Filled.Email),
                    BottomNavigationItem(title = "Настройки", icon = Icons.Filled.Settings),
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
                        onClick = {},
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
)
