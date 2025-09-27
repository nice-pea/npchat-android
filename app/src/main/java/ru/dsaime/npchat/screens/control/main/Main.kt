package ru.dsaime.npchat.screens.control.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ru.dsaime.npchat.ui.components.LeftButton
import ru.dsaime.npchat.ui.components.dialog.BottomDialogHeader
import ru.dsaime.npchat.ui.components.dialog.BottomDialogParams

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlDialog(
    params: BottomDialogParams,
    onNavigationRequest: (ControlEffect.Navigation) -> Unit,
) {
    BottomDialogHeader("Управление", params)
    LeftButton("Создать чат", { onNavigationRequest(ControlEffect.Navigation.CreateChat) })
    LeftButton("Профиль", { onNavigationRequest(ControlEffect.Navigation.Profile) })
}

sealed interface ControlEffect {
    sealed interface Navigation : ControlEffect {
        object CreateChat : Navigation

        object Profile : Navigation
    }
}
