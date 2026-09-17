package com.bazyak.walletplus.ui.components.pass.pkpass.ticket

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Custom ticket-shaped outline with rounded corners and semicircular notches.
 * Creates the classic perforated ticket appearance.
 *
 * @param cornerRadius Radius for the four corners
 * @param notchPosition Position of the notches as a percentage of height (0.0 to 1.0)
 */
class TicketShape(private val cornerRadius: Dp = 16.dp, private val notchPosition: Float = 0.6f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val notchY = size.height * notchPosition

        return Outline.Generic(
            path = Path().apply {
                // clockwise
                moveTo(0f, cornerRadiusPx)

                arcTo(
                    rect = Rect(0f, 0f, cornerRadiusPx * 2, cornerRadiusPx * 2),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                lineTo(size.width - cornerRadiusPx, 0f)

                arcTo(
                    rect = Rect(
                        size.width - cornerRadiusPx * 2,
                        0f,
                        size.width,
                        cornerRadiusPx * 2,
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                lineTo(size.width, notchY - cornerRadiusPx * 2)

                arcTo(
                    rect = Rect(
                        size.width - cornerRadiusPx * 2,
                        notchY - cornerRadiusPx * 2,
                        size.width,
                        notchY,
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                arcTo(
                    rect = Rect(
                        size.width - cornerRadiusPx * 2,
                        notchY,
                        size.width,
                        notchY + cornerRadiusPx * 2,
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                lineTo(size.width, size.height - cornerRadiusPx * 2)

                arcTo(
                    rect = Rect(
                        size.width - cornerRadiusPx * 2,
                        size.height - cornerRadiusPx * 2,
                        size.width,
                        size.height,
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                lineTo(0f - cornerRadiusPx * 2, size.height)

                arcTo(
                    rect = Rect(
                        0f,
                        size.height - cornerRadiusPx * 2,
                        0f + cornerRadiusPx * 2,
                        size.height,
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                lineTo(0f, size.height - cornerRadiusPx * 2)

                arcTo(
                    rect = Rect(
                        0f,
                        notchY,
                        0f + cornerRadiusPx * 2,
                        notchY + cornerRadiusPx * 2,
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                arcTo(
                    rect = Rect(
                        0f,
                        notchY - cornerRadiusPx * 2,
                        0f + cornerRadiusPx * 2,
                        notchY,
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )

                close()
            },
        )
    }
}
