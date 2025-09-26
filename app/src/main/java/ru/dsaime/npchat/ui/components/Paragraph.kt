package ru.dsaime.npchat.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.dsaime.npchat.ui.theme.Dp8
import ru.dsaime.npchat.ui.theme.Font

@Composable
fun Paragraph(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier =
            Modifier
                .padding(Dp8)
                .then(modifier),
        style = Font.Text16W400,
    )
}
