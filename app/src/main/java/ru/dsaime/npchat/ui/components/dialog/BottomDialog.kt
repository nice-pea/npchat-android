package ru.dsaime.npchat.ui.components.dialog

import android.content.ClipData
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.functions.runSuspend
import ru.dsaime.npchat.ui.components.Gap
import ru.dsaime.npchat.ui.modifiers.fadeIn
import ru.dsaime.npchat.ui.theme.ColorBG
import ru.dsaime.npchat.ui.theme.ColorScrim
import ru.dsaime.npchat.ui.theme.ColorText
import ru.dsaime.npchat.ui.theme.Dp16
import ru.dsaime.npchat.ui.theme.Dp2
import ru.dsaime.npchat.ui.theme.Dp8
import ru.dsaime.npchat.ui.theme.Font

@Composable
fun BottomDialogProperty(property: BottomDialogProperty) {
    Row {
        Text(property.name, style = Font.Text16W600)
        Gap(Dp8)
        if (property.action != null) {
            Text(
                property.value,
                modifier =
                    Modifier
                        .clickable(onClick = property.action),
                style = Font.Property16W400,
            )
        } else {
            val clipboard = LocalClipboard.current
            val coroutineScope = rememberCoroutineScope()
            Text(
                property.value,
                style = Font.Text16W400,
                modifier =
                    Modifier.clickable {
                        coroutineScope.launch {
                            ClipData
                                .newPlainText(property.name, property.value)
                                .toClipEntry()
                                .runSuspend(clipboard::setClipEntry)
                        }
                    },
            )
        }
    }
}

@Composable
fun BottomDialogProperties(vararg properties: BottomDialogProperty) {
    Column(
        modifier =
            Modifier
                .padding(horizontal = Dp8)
                .padding(bottom = Dp16),
        verticalArrangement = Arrangement.spacedBy(Dp2),
    ) {
        properties.forEach {
            BottomDialogProperty(it)
        }
    }
}

data class BottomDialogProperty(
    val name: String,
    val value: String,
    val action: (() -> Unit)? = null,
)

data class BottomDialogParams(
    val canPopUp: Boolean,
    val onPopUp: () -> Unit,
)

@Composable
fun BottomDialogHeader(
    title: String,
    params: BottomDialogParams,
) {
    Row(
        modifier =
            Modifier
                .padding(bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (params.canPopUp) {
            PopUpButton(params.onPopUp)
        }
        Text(title, style = Font.Text18W400, modifier = Modifier.fadeIn(200, .6f))
    }
}

@Composable
private fun PopUpButton(onClick: () -> Unit) {
    Text(
        text = "<-",
        modifier =
            Modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .padding(start = 5.dp, end = 15.dp),
        style = Font.Text18W400,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomDialog(
    isVisibleRequired: Boolean,
    onClosed: () -> Unit,
    skipPartiallyExpanded: Boolean = true,
    sheet: @Composable ColumnScope.(dismissRequest: suspend () -> Unit) -> Unit,
) {
    val state =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
        )
    BottomDialog(
        isVisibleRequired = isVisibleRequired,
        onClosed = onClosed,
        state = state,
    ) {
        sheet {
            state.hide()
            onClosed()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomDialog(
    isVisibleRequired: Boolean,
    onClosed: () -> Unit,
    state: SheetState = rememberModalBottomSheetState(),
    sheet: @Composable ColumnScope.() -> Unit,
) {
    LaunchedEffect(isVisibleRequired) {
        if (isVisibleRequired) {
            state.show()
        } else {
            state.hide()
        }
    }

    if (state.isVisible || state.targetValue != SheetValue.Hidden) {
        ModalBottomSheet(
            shape = RectangleShape,
            onDismissRequest = onClosed,
            sheetState = state,
            containerColor = Color.Transparent, // Прозрачный фон обертки
            scrimColor = ColorScrim,
            tonalElevation = 0.dp, // Убираем тень, если нужно
            dragHandle = null, // Убираем хэндл, если не нужен
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .background(ColorBG)
                        .border(1.dp, ColorText)
                        .padding(20.dp)
                        .animateContentSize(tween()),
            ) {
                sheet()
            }
        }
    }
}
