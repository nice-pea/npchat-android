package ru.dsaime.npchat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.dsaime.npchat.model.Host
import ru.dsaime.npchat.ui.theme.ColorHostIncompatible
import ru.dsaime.npchat.ui.theme.ColorHostOffline
import ru.dsaime.npchat.ui.theme.ColorHostOnline
import ru.dsaime.npchat.ui.theme.ColorHostUnknown

@Composable
fun HostStatusIcon(
    status: Host.Status,
    modifier: Modifier = Modifier,
) {
    when (status) {
        Host.Status.ONLINE -> Icon(Icons.Filled.Check, "online", modifier, tint = ColorHostOnline)
        Host.Status.OFFLINE -> Icon(Icons.Filled.Close, "offline", modifier, tint = ColorHostOffline)
        Host.Status.INCOMPATIBLE -> Icon(Icons.Filled.Warning, "incompatible", modifier, tint = ColorHostIncompatible)
        Host.Status.UNKNOWN -> Icon(Icons.Filled.Info, "unknown", modifier, tint = ColorHostUnknown)
    }
}
