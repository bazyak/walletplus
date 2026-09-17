package com.bazyak.walletplus.ui.components.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * A perforation-style divider that creates evenly spaced circular dots.
 *
 * Creates the classic perforated ticket appearance by drawing dots across the width.
 * Height is 0dp because dots are centered on the divider line and extend above/below it.
 *
 * @param modifier Modifier to be applied to the canvas
 * @param dotRadius Radius of each dot in pixels (default: 8f)
 * @param dotColor Color of the dots (default: onSurface from theme)
 */
@Composable
fun DottedDivider(modifier: Modifier = Modifier, dotRadius: Float = 8f, dotColor: Color = Color.Black) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(0.dp),
    ) {
        val canvasWidth = size.width
        if (canvasWidth <= 0f || dotRadius <= 0f) return@Canvas

        val targetSpacing = dotRadius * 4f
        val numDots = max(2, (canvasWidth / targetSpacing).toInt() + 1)
        val actualSpacing = canvasWidth / (numDots - 1)

        repeat(numDots) { i ->
            val x = i * actualSpacing
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(x, 0f),
            )
        }
    }
}
