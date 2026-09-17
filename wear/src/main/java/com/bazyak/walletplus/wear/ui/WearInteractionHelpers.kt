package com.bazyak.walletplus.wear.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
internal fun ScrollAffordance(unfoldProgress: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(width = 34.dp, height = 18.dp)
            .alpha(0.5f),
    ) {
        val strokeWidth = 3.dp.toPx()
        val arrowLeft = Offset(size.width * 0.28f, size.height * 0.62f)
        val arrowTip = Offset(size.width * 0.5f, size.height * 0.32f)
        val arrowRight = Offset(size.width * 0.72f, size.height * 0.62f)
        val lineLeft = Offset(size.width * 0.24f, size.height * 0.5f)
        val lineCenter = Offset(size.width * 0.5f, size.height * 0.5f)
        val lineRight = Offset(size.width * 0.76f, size.height * 0.5f)
        val progress = unfoldProgress.coerceIn(0f, 1f)

        drawLine(
            color = color,
            start = lerpOffset(arrowLeft, lineLeft, progress),
            end = lerpOffset(arrowTip, lineCenter, progress),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = lerpOffset(arrowTip, lineCenter, progress),
            end = lerpOffset(arrowRight, lineRight, progress),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
internal fun rememberWearPassHeaderTouchScrollConnection(
    pagerState: PagerState,
    headerScrollState: ScrollState,
    headerPageIndex: Int,
): NestedScrollConnection = remember(pagerState, headerScrollState, headerPageIndex) {
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || !pagerState.isPageAlignedAt(headerPageIndex)) {
                return Offset.Zero
            }

            val consumed = headerScrollState.scrollByTouchDelta(available.y)
            return if (consumed == 0f) Offset.Zero else Offset(x = 0f, y = -consumed)
        }
    }
}

internal fun PagerState.isPageSettledAt(index: Int): Boolean = !isScrollInProgress && isPageAlignedAt(index)

internal fun PagerState.pagePosition(): Float =
    (currentPage + currentPageOffsetFraction).coerceIn(0f, (pageCount - 1).coerceAtLeast(0).toFloat())

private fun PagerState.isPageAlignedAt(index: Int): Boolean =
    currentPage == index && abs(currentPageOffsetFraction) <= SETTLED_PAGE_TOLERANCE

private fun ScrollState.scrollByTouchDelta(availableY: Float): Float {
    val scrollDelta = -availableY
    val canConsumeDelta = when {
        scrollDelta > 0f -> canScrollForward
        scrollDelta < 0f -> canScrollBackward
        else -> false
    }
    if (!canConsumeDelta) return 0f
    return dispatchRawDelta(scrollDelta)
}

private const val SETTLED_PAGE_TOLERANCE = 0.001f

@Composable
internal fun KeepScreenBrightness(isEnabled: Boolean, brightness: Float) {
    val activity = LocalContext.current.findActivity()
    val originalBrightness = remember(activity) {
        activity?.window?.attributes?.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }

    LaunchedEffect(activity, isEnabled, brightness, originalBrightness) {
        activity?.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.screenBrightness = if (isEnabled) brightness else originalBrightness
            window.attributes = layoutParams
        }
    }

    DisposableEffect(activity, originalBrightness) {
        onDispose {
            activity?.window?.let { window ->
                val layoutParams = window.attributes
                layoutParams.screenBrightness = originalBrightness
                window.attributes = layoutParams
            }
        }
    }
}

@Composable
internal fun KeepScreenOn(isEnabled: Boolean) {
    val activity = LocalContext.current.findActivity()

    DisposableEffect(activity, isEnabled) {
        val window = activity?.window
        val keepScreenOnFlag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        val hadKeepScreenOn = (window?.attributes?.flags?.and(keepScreenOnFlag) ?: 0) != 0

        if (isEnabled) {
            window?.addFlags(keepScreenOnFlag)
        }

        onDispose {
            if (isEnabled && !hadKeepScreenOn) {
                window?.clearFlags(keepScreenOnFlag)
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun lerpOffset(start: Offset, end: Offset, fraction: Float): Offset = Offset(
    x = start.x + (end.x - start.x) * fraction,
    y = start.y + (end.y - start.y) * fraction,
)
