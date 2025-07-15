package ru.dsaime.npchat.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.dsaime.npchat.ui.theme.White

@Composable
fun Progress(
    modifier: Modifier = Modifier,
) {
    CircularProgressIndicator(
        modifier = Modifier.then(modifier),
        color = White,
    )
}

@Composable
fun ProgressMax(
    modifier: Modifier = Modifier,
) {
    Progress(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
    )
}
