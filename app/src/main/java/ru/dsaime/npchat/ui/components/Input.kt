package ru.dsaime.npchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.dsaime.npchat.ui.theme.Black
import ru.dsaime.npchat.ui.theme.Dp1
import ru.dsaime.npchat.ui.theme.Dp10
import ru.dsaime.npchat.ui.theme.Dp2
import ru.dsaime.npchat.ui.theme.Dp20
import ru.dsaime.npchat.ui.theme.Dp6
import ru.dsaime.npchat.ui.theme.Dp8
import ru.dsaime.npchat.ui.theme.Font
import ru.dsaime.npchat.ui.theme.White
import ru.dsaime.npchat.ui.theme.cursorBrush


@Preview
@Composable
private fun PreviewInputState() {
    Input(
        modifier = Modifier
            .background(Black)
            .padding(Dp20),
        title = "Title",
        placeholder = "Empty",
        helperText = "Login using for login in your profile without  other credential, it sensitive information, don’t share it",
        textFieldState = rememberTextFieldState(initialText = "Input text")
    )
}

@Preview
@Composable
private fun PreviewInput() {
    Input(
        modifier = Modifier
            .background(Black)
            .padding(Dp20),
        title = "Title",
        placeholder = "Empty",
        helperText = "Login using for login in your profile without  other credential, it sensitive information, don’t share it",
        value = "Input text",
        onValueChange = {},
    )
}

@Composable
fun Input(
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String,
    helperText: String = "",
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.then(modifier)
    ) {
        if (title != "") {
            Text(title, style = Font.White12W500)
            Gap(Dp6)
        }
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .background(Black)
                .border(Dp1, White)
                .padding(Dp10),
            value = value,
            onValueChange = onValueChange,
            cursorBrush = cursorBrush,
            textStyle = Font.White16W400,
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(placeholder, style = Font.GrayCharcoal16W400)
                }
                innerTextField()
            }
        )
        if (helperText != "") {
            Gap(Dp2)
            Text(helperText, style = Font.GrayCharcoal12W400)
        }
        Gap(Dp8)
    }
}

@Composable
fun Input(
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String,
    helperText: String = "",
    textFieldState: TextFieldState,
) {
    Column(
        modifier = Modifier.then(modifier)
    ) {
        if (title != "") {
            Text(title, style = Font.White12W500)
            Gap(Dp6)
        }
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .background(Black)
                .border(Dp1, White)
                .padding(Dp10),
            state = textFieldState,
            cursorBrush = cursorBrush,
            textStyle = Font.White16W400,
            decorator = { innerTextField ->
                if (textFieldState.text == "") {
                    Text(placeholder, style = Font.GrayCharcoal16W400)
                }
                innerTextField()
            }
        )
        if (helperText != "") {
            Gap(Dp2)
            Text(helperText, style = Font.GrayCharcoal12W400)
        }
        Gap(Dp8)
    }
}