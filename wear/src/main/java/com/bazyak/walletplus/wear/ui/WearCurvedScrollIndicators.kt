package com.bazyak.walletplus.wear.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val INDICATOR_HIDE_DELAY_MILLIS = 900L
private const val QR_TOP_ANGLE = -3.25f
private const val QR_BOTTOM_ANGLE = 4.25f
private const val HEADER_DOT_ANGLE = -40f
private const val TRACK_START_ANGLE = -26f
private const val TRACK_SWEEP_ANGLE = 58f
private const val THUMB_SWEEP_ANGLE = 18f
private const val ACTIVE_DOT_ALPHA = 0.78f
private const val TRACK_ALPHA = 0.18f

@Composable
internal fun PassCurvedScrollIndicator(
    pageProgress: Float,
    headerScrollState: ScrollState,
    headerColor: Color,
    modifier: Modifier = Modifier,
) {
    var keepHeaderIndicatorVisible by remember { mutableStateOf(true) }
    val isMoving = pageProgress < 0.98f || headerScrollState.isScrollInProgress

    LaunchedEffect(isMoving, pageProgress, headerScrollState.value) {
        if (isMoving) {
            keepHeaderIndicatorVisible = true
        } else {
            delay(INDICATOR_HIDE_DELAY_MILLIS)
            keepHeaderIndicatorVisible = false
        }
    }

    val visibilityProgress by animateFloatAsState(
        targetValue = if (pageProgress < 0.98f || keepHeaderIndicatorVisible) 1f else 0f,
        label = "pass-scroll-indicator-alpha",
    )
    if (visibilityProgress <= 0.01f) return

    CurvedScrollIndicator(
        pageProgress = pageProgress,
        scrollProgress = headerScrollState.scrollProgress(),
        color = lerp(Color.Black, headerColor, pageProgress.coerceIn(0f, 1f)),
        visibilityProgress = visibilityProgress,
        showTopDot = true,
        modifier = modifier,
    )
}

@Composable
internal fun ListCurvedScrollIndicator(
    listState: ScalingLazyListState,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
) {
    if (listState.layoutInfo.totalItemsCount <= 1) return

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(listState.isScrollInProgress, listState.centerItemIndex, listState.centerItemScrollOffset) {
        if (listState.isScrollInProgress) {
            isVisible = true
        } else {
            delay(INDICATOR_HIDE_DELAY_MILLIS)
            isVisible = false
        }
    }

    val visibilityProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = "list-scroll-indicator-alpha",
    )
    if (visibilityProgress <= 0.01f) return

    CurvedScrollIndicator(
        pageProgress = 1f,
        scrollProgress = listState.scrollProgress(),
        color = color,
        visibilityProgress = visibilityProgress,
        showTopDot = false,
        modifier = modifier,
    )
}

@Composable
private fun CurvedScrollIndicator(
    pageProgress: Float,
    scrollProgress: Float,
    color: Color,
    visibilityProgress: Float,
    showTopDot: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val progress = pageProgress.coerceIn(0f, 1f)
        val barProgress = progress * visibilityProgress.coerceIn(0f, 1f)
        val radius = minOf(size.width, size.height) / 2f - 12.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val dotRadius = 3.dp.toPx()
        val trackWidth = 4.dp.toPx()
        val thumbWidth = lerpFloat(dotRadius * 2f, trackWidth, barProgress)
        val thumbSweep = lerpFloat(0.1f, THUMB_SWEEP_ANGLE, barProgress)
        val thumbCenterAngle = TRACK_START_ANGLE +
            (TRACK_SWEEP_ANGLE - THUMB_SWEEP_ANGLE) * scrollProgress.coerceIn(0f, 1f) +
            THUMB_SWEEP_ANGLE / 2f
        val lowerCenterAngle = lerpFloat(QR_BOTTOM_ANGLE, thumbCenterAngle, progress)
        val lowerStartAngle = lowerCenterAngle - thumbSweep / 2f
        val trackEndAngle = TRACK_START_ANGLE + TRACK_SWEEP_ANGLE
        val isStateTransition = showTopDot && progress < 0.999f
        val trackStartAngle = if (isStateTransition) lowerStartAngle else TRACK_START_ANGLE
        val trackSweepAngle = if (isStateTransition) {
            (trackEndAngle - trackStartAngle).coerceAtLeast(0f) * barProgress
        } else {
            TRACK_SWEEP_ANGLE * visibilityProgress
        }
        val topPoint = pointOnCircle(
            center = center,
            radius = radius,
            angleDegrees = lerpFloat(QR_TOP_ANGLE, HEADER_DOT_ANGLE, progress),
        )
        val topDotAlpha = lerpFloat(ACTIVE_DOT_ALPHA, TRACK_ALPHA, progress) * visibilityProgress

        if (showTopDot) {
            drawCircle(
                color = color.copy(alpha = topDotAlpha),
                radius = dotRadius,
                center = topPoint,
            )
        }

        drawArc(
            color = color.copy(alpha = TRACK_ALPHA * barProgress),
            startAngle = trackStartAngle,
            sweepAngle = trackSweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = trackWidth, cap = StrokeCap.Round),
        )

        drawArc(
            color = color.copy(alpha = lerpFloat(0.28f, 0.82f, progress) * visibilityProgress),
            startAngle = lowerStartAngle,
            sweepAngle = thumbSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = thumbWidth, cap = StrokeCap.Round),
        )
    }
}

private fun ScrollState.scrollProgress(): Float = if (maxValue == 0) {
    0f
} else {
    (value.toFloat() / maxValue).coerceIn(0f, 1f)
}

private fun ScalingLazyListState.scrollProgress(): Float {
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems <= 1) return 0f

    val averageItemSize = layoutInfo.visibleItemsInfo
        .takeIf { it.isNotEmpty() }
        ?.map { it.unadjustedSize }
        ?.average()
        ?.toFloat()
        ?.takeIf { it > 0f }
        ?: return centerItemIndex.toFloat() / (totalItems - 1)

    return ((centerItemIndex + centerItemScrollOffset / averageItemSize) / (totalItems - 1))
        .coerceIn(0f, 1f)
}

private fun pointOnCircle(center: Offset, radius: Float, angleDegrees: Float): Offset {
    val angleRadians = angleDegrees * PI.toFloat() / 180f
    return Offset(
        x = center.x + radius * cos(angleRadians),
        y = center.y + radius * sin(angleRadians),
    )
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)
