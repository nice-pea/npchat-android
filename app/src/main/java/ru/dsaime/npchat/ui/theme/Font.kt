package ru.dsaime.npchat.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.dsaime.npchat.R

object Font {
    private val interFontFamily = FontFamily(Font(R.font.inter))
    private val S12W500 =
        TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.W500,
            fontFamily = interFontFamily,
        )
    private val S12W400 =
        TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.W400,
            fontFamily = interFontFamily,
        )
    private val S14W400 =
        TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            fontFamily = interFontFamily,
        )
    private val S14W500 =
        TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.W500,
            fontFamily = interFontFamily,
        )
    private val S16W400 =
        TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            fontFamily = interFontFamily,
        )
    private val S16W600 =
        TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.W600,
            fontFamily = interFontFamily,
        )
    private val S18W400 =
        TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.W400,
            fontFamily = interFontFamily,
        )
    private val S20W400 =
        TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.W400,
            fontFamily = interFontFamily,
        )

    // White
    val White12W500 = S12W500.copy(White)
    val White12W400 = S12W400.copy(White)
    val White14W400 = S14W400.copy(White)
    val White16W400 = S16W400.copy(White)
    val White16W400Italic = S16W400.copy(White, fontStyle = FontStyle.Italic)
    val White18W400 = S18W400.copy(White)
    val White20W400 = S20W400.copy(White)

    // GrayCharcoal
    @Deprecated("")
    val GrayCharcoal12W400 = S12W400.copy(GrayCharcoal)

    @Deprecated("")
    val GrayCharcoal12W400Italic = S12W400.copy(GrayCharcoal, fontStyle = FontStyle.Italic)

    @Deprecated("")
    val GrayCharcoal16W400 = S16W400.copy(GrayCharcoal)

    // Pink
    @Deprecated("")
    val Pink14W500Italic = S14W500.copy(Pink, fontStyle = FontStyle.Italic)

    // BlueSky
    @Deprecated("")
    val BlueSky14W400 = S14W400.copy(BlueSky)

    // BlueGraph
    @Deprecated("")
    val BlueGraph12W400 = S12W400.copy(BlueGraph)

    @Deprecated("")
    val BlueGraph14W500 = S14W500.copy(BlueGraph)

    // DarkGraph
    @Deprecated("")
    val DarkGraph14W500Italic = S14W500.copy(DarkGraph, fontStyle = FontStyle.Italic)

    @Deprecated("")
    val DarkGraph12W400 = S12W400.copy(DarkGraph)

    // ColorDeleted
    val Deleted14W500 = S14W500.copy(ColorDeleted)
    val Deleted14W400 = S14W400.copy(ColorDeleted)

    // ColorSender
    val Sender12W400 = S12W400.copy(ColorSender)

    // ColorText
    val Text14W400 = S14W400.copy(ColorText)
    val Text12W400 = S12W400.copy(ColorText)
    val Text18W400 = S18W400.copy(ColorText)
    val Text16W600 = S16W600.copy(ColorText)
    val Text16W400 = S16W400.copy(ColorText)

    // ColorAction
    val Action14W400 = S14W400.copy(ColorAction)

    // ColorProperty
    val Property16W400 = S16W400.copy(ColorProperty)
}
