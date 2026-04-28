package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.MEDIUM_CORNER_RADIUS

@Composable
internal fun SongbookCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    cardStyle: SongbookCardStyle = defaultSongbookCardStyle(),
    content: @Composable BoxScope.() -> Unit,
) {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        offset.animateTo(
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = LinearEasing),
            ),
        )
    }

    Box(
        modifier = modifier
            .clip(cardStyle.shape)
            .background(
                color = cardStyle.containerColor,
                shape = cardStyle.shape,
            )
            .then(
                if (cardStyle is SongbookCardStyle.Dashed) {
                    Modifier.drawWithCache {
                        val style = Stroke(
                            width = DEFAULT_BORDER_WIDTH.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(10f, 10f),
                                phase = offset.value,
                            ),
                        )
                        val cornerRadius = CornerRadius(
                            x = cardStyle.cornerRadius.toPx(),
                            y = cardStyle.cornerRadius.toPx(),
                        )

                        onDrawBehind {
                            drawRoundRect(
                                color = cardStyle.outlineColor,
                                style = style,
                                cornerRadius = cornerRadius,
                            )
                        }
                    }
                } else {
                    Modifier
                },
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                role = Role.Button,
            ),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Composable
internal fun defaultSongbookCardStyle() = SongbookCardStyle.Filled(
    shape = MaterialTheme.shapes.medium,
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
)

@Composable
internal fun dashedSongbookCardStyle() = SongbookCardStyle.Dashed(
    outlineColor = MaterialTheme.colorScheme.outline,
    cornerRadius = MEDIUM_CORNER_RADIUS,
    containerColor = Color.Transparent,
)

@Stable
internal sealed interface SongbookCardStyle {
    val containerColor: Color
    val shape: Shape

    @Stable
    data class Filled(
        override val shape: Shape,
        override val containerColor: Color,
    ) : SongbookCardStyle

    @Stable
    data class Dashed(
        val outlineColor: Color,
        val cornerRadius: Dp,
        override val containerColor: Color,
    ) : SongbookCardStyle {
        override val shape = RoundedCornerShape(cornerRadius)
    }
}
