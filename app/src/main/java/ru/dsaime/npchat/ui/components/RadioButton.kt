package ru.dsaime.npchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import ru.dsaime.npchat.ui.theme.ColorBG
import ru.dsaime.npchat.ui.theme.ColorHostOffline
import ru.dsaime.npchat.ui.theme.DarkGray
import ru.dsaime.npchat.ui.theme.Dp10
import ru.dsaime.npchat.ui.theme.Dp16
import ru.dsaime.npchat.ui.theme.Dp2
import ru.dsaime.npchat.ui.theme.Dp8
import ru.dsaime.npchat.ui.theme.Font
import ru.dsaime.npchat.ui.theme.RoundMin
import ru.dsaime.npchat.ui.theme.White

@Composable
@Preview
private fun RadioButtonPreview() {
    Column {
        RadioButton(
            text = "Text",
            onClick = {},
            selected = false,
            helperText = "Helper Text",
        )
        RadioButton(
            text = "Text",
            onClick = {},
            selected = true,
            helperText = "The number of chats that can be created is limited. Created chats cannot be deleted",
        )
        RadioButton(
            text = "Text",
            onClick = {},
            selected = false,
        )
        RadioButton(
            text = "Text",
            onClick = {},
            selected = true,
        )
        RadioButton(
            text = "Text",
            onClick = {},
            selected = true,
            icon = {
                Icon(Icons.Filled.Delete, null, tint = ColorHostOffline)
            },
        )
    }
}

@Composable
fun RadioButton(
    text: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    helperText: String = "",
) {
    Column(
        modifier =
            Modifier
                .background(ColorBG)
                .fillMaxWidth()
                .clip(RoundMin)
                .clickable(onClick = onClick)
                .padding(Dp8)
                .then(modifier),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dp10),
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .size(Dp10)
                    .background(if (selected) White else DarkGray),
            )
            if (icon != null) {
                Box(Modifier.heightIn(max = Dp16)) {
                    icon()
                }
            }
            Text(text, style = Font.Text14W400, modifier = Modifier.weight(1f))
        }
        if (helperText != "") {
            Gap(Dp2)
            Text(helperText, style = Font.Text12W400)
        }
    }
}
