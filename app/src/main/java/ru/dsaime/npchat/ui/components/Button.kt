package ru.dsaime.npchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ru.dsaime.npchat.ui.theme.Black
import ru.dsaime.npchat.ui.theme.Dp2
import ru.dsaime.npchat.ui.theme.Dp20
import ru.dsaime.npchat.ui.theme.Dp8
import ru.dsaime.npchat.ui.theme.Font
import ru.dsaime.npchat.ui.theme.RoundMin

@Preview
@Composable
private fun PreviewButton() {
    LeftButton(
        modifier =
            Modifier
                .background(Black)
                .padding(Dp20),
        text = "Confirm",
        helperText = "The number of chats that can be created is limited. Created chats cannot be deleted",
        onClick = {},
        isRight = true,
    )
}

@Composable
fun LeftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    helperText: String = "",
    isRight: Boolean = false,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundMin)
                .clickable(onClick = onClick)
                .padding(Dp8)
                .then(modifier),
        horizontalAlignment = if (isRight) Alignment.End else Alignment.Start,
    ) {
        Text(text, style = Font.Text16W600)
        if (helperText != "") {
            Gap(Dp2)
            Text(helperText, style = Font.Text12W400, textAlign = if (isRight) TextAlign.Right else TextAlign.Left)
        }
    }
}
