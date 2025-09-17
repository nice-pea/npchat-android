package ru.dsaime.npchat.screens.home

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import ru.dsaime.npchat.ui.theme.ColorText

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
        topBar = {
            TopAppBar(
                title = { Text("Все чаты") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = ColorBG,
                        titleContentColor = ColorText,
                    ),
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description",
                            tint = ColorText,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Localized description",
                            tint = ColorText,
                        )
                    }
                },
            )
        },
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
//                IconButton(onClick = { /* do something */ }) {
//                    Icon(Icons.Filled.Notifications, contentDescription = "Localized description")
//                }
//                IconButton(onClick = { /* do something */ }) {
//                    Icon(Icons.Filled.Email, contentDescription = "Localized description")
//                }
//                IconButton(onClick = { /* do something */ }) {
//                    Icon(Icons.Filled.Settings, contentDescription = "Localized description")
//                }
            }
//            BottomAppBar(
//                modifier = Modifier
//                    .height(50.dp)
//                    .topBorder(ColorPart, 1f)
//                ,
//                containerColor = ColorBG,
//                contentColor = ColorPart,
//                actions = {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceAround,
//                        verticalAlignment = Alignment.CenterVertically,
//                    ){
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(Icons.Filled.Notifications, contentDescription = "Localized description")
//                        }
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(Icons.Filled.Email, contentDescription = "Localized description")
//                        }
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(Icons.Filled.Settings, contentDescription = "Localized description")
//                        }
//                    }
//                },
//            )
        },
    ) {
        it
        content()
    }
}

data class BottomNavigationItem(
    val title: String,
    val icon: ImageVector,
)
