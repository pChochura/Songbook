package com.pointlessapps.songbook.preview.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

@Composable
internal fun PinchToZoomBox(
    interactionSource: MutableInteractionSource,
    onZoomChanged: (zoomChange: Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        onZoomChanged(zoomChange)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.size > 1) {
                            interactionSource.tryEmit(PinchToZoomInteraction.Start)
                        }
                    } while (event.changes.any { it.pressed })
                    interactionSource.tryEmit(PinchToZoomInteraction.Stop)
                }
            }
            .transformable(state = transformableState, canPan = { false }),
        contentAlignment = Alignment.TopCenter,
        content = content,
    )
}

internal interface PinchToZoomInteraction : Interaction {
    data object Start : PinchToZoomInteraction
    data object Stop : PinchToZoomInteraction
}

@Composable
internal fun InteractionSource.collectIsPinchedToZoomAsState(): State<Boolean> {
    val isDragged = remember { mutableStateOf(false) }
    LaunchedEffect(this) {
        interactions.collect { interaction ->
            when (interaction) {
                is PinchToZoomInteraction.Start -> isDragged.value = true
                is PinchToZoomInteraction.Stop -> isDragged.value = false
            }
        }
    }

    return isDragged
}
