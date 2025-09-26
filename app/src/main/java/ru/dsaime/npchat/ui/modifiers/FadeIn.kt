package ru.dsaime.npchat.ui.modifiers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha

fun Modifier.fadeIn(
    durationMillis: Int = 300,
    from: Float = 0f,
): Modifier =
    composed {
        val animatedFloat = remember(from) { Animatable(from) }
        LaunchedEffect(Unit) {
            animatedFloat.animateTo(1f, animationSpec = tween(durationMillis = durationMillis))
        }

        this.then(Modifier.alpha(animatedFloat.value))
    }
