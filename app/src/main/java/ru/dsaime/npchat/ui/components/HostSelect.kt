package ru.dsaime.npchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.dsaime.npchat.model.Host
import ru.dsaime.npchat.ui.theme.Black
import ru.dsaime.npchat.ui.theme.ColorHostIncompatible
import ru.dsaime.npchat.ui.theme.ColorHostOffline
import ru.dsaime.npchat.ui.theme.ColorHostOnline
import ru.dsaime.npchat.ui.theme.ColorHostUnknown
import ru.dsaime.npchat.ui.theme.ColorText
import ru.dsaime.npchat.ui.theme.Dp1
import ru.dsaime.npchat.ui.theme.Dp10
import ru.dsaime.npchat.ui.theme.Dp16
import ru.dsaime.npchat.ui.theme.Dp24
import ru.dsaime.npchat.ui.theme.Dp6
import ru.dsaime.npchat.ui.theme.Dp8
import ru.dsaime.npchat.ui.theme.Font

@Preview(showBackground = true, backgroundColor = 0)
@Composable
private fun HostSelectPreview() {
    val fakeHosts =
        listOf(
            Host(url = "https://main.example.com:7877", status = Host.Status.ONLINE),
            Host(url = "https://test.example.com/api", status = Host.Status.OFFLINE),
            Host(url = "https://cloud.example.com/v2", status = Host.Status.INCOMPATIBLE),
            Host(url = "https://api.example.com", status = Host.Status.UNKNOWN),
            Host(url = "https://api.example.com/v2/api/hostname/path/resource", status = Host.Status.UNKNOWN),
            Host(url = "httpsapiexamplecomvapihostnamepathresource", status = Host.Status.UNKNOWN),
        )
    Column(
        verticalArrangement = Arrangement.spacedBy(Dp10),
    ) {
        fakeHosts.forEach {
            HostSelect(
                host = it,
                onClick = {},
                onCheckConn = {},
            )
        }
    }
}

@Composable
fun HostSelect(
    host: Host?,
    onClick: () -> Unit,
    onCheckConn: () -> Unit,
) {
    Column {
        Text("Сервер", style = Font.Text12W400)
        Gap(Dp6)
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HostField(
                host = host,
                onClickSelect = onClick,
                modifier = Modifier.weight(1f),
            )
            StatusButton(
                onClick = onCheckConn,
                status = host?.status ?: Host.Status.UNKNOWN,
            )
        }
        Gap(Dp8)
    }
}

@Composable
private fun StatusButton(
    onClick: () -> Unit,
    status: Host.Status,
) {
    val modifier = Modifier.size(Dp24)
    IconButton(onClick = onClick) {
        when (status) {
            Host.Status.ONLINE -> Icon(Icons.Filled.Check, "online", modifier, tint = ColorHostOnline)
            Host.Status.OFFLINE -> Icon(Icons.Filled.Close, "offline", modifier, tint = ColorHostOffline)
            Host.Status.INCOMPATIBLE -> Icon(Icons.Filled.Warning, "incompatible", modifier, tint = ColorHostIncompatible)
            Host.Status.UNKNOWN -> Icon(Icons.Filled.Info, "unknown", modifier, tint = ColorHostUnknown)
        }
    }
}

@Composable
private fun HostField(
    host: Host?,
    onClickSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClickSelect)
                .background(Black)
                .border(Dp1, ColorText)
                .padding(Dp10)
                .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SelectionContainer(Modifier.weight(1f)) {
            Text(host?.url.orEmpty(), style = Font.Text16W400)
        }
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "select host",
            tint = ColorText,
            modifier = Modifier.size(Dp16),
        )
    }
}
