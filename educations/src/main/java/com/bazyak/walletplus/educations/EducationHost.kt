package com.bazyak.walletplus.educations

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R as StringsR
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun EducationHost(
    activeEducation: ActiveEducation?,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = activeEducation ?: return
    val registry = LocalEducationTargetRegistry.current
    val targetBounds = active.step.targetKey?.let { registry?.boundsFor(it) }
        ?.takeIf { active.step.placement == EducationStepPlacement.NearTarget }

    BackHandler {
        if (active.canGoBack) {
            onBack()
        } else {
            onFinish()
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val bubbleWidth = if (maxWidth < 372.dp) {
            maxWidth - 32.dp
        } else {
            340.dp
        }
        val bubbleHeightEstimatePx = with(density) { 220.dp.toPx() }
        val bubbleOffset = remember(targetBounds, maxWidthPx, maxHeightPx) {
            calculateBubbleOffset(
                targetBounds = targetBounds,
                maxWidthPx = maxWidthPx,
                maxHeightPx = maxHeightPx,
                bubbleWidthPx = with(density) { bubbleWidth.toPx() },
                bubbleHeightPx = bubbleHeightEstimatePx,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onFinish() })
                },
        )

        EducationScrim(targetBounds = targetBounds)

        targetBounds?.let { TargetGlow(targetBounds = it) }

        Box(
            modifier = if (targetBounds == null || active.step.placement == EducationStepPlacement.Center) {
                Modifier.align(Alignment.Center)
            } else {
                Modifier.offset {
                    IntOffset(
                        x = bubbleOffset.x.roundToInt(),
                        y = bubbleOffset.y.roundToInt(),
                    )
                }
            },
        ) {
            EducationCard(
                activeEducation = active,
                onNext = onNext,
                onBack = onBack,
                onFinish = onFinish,
                modifier = Modifier.widthIn(max = bubbleWidth),
            )
        }
    }
}

@Composable
private fun EducationScrim(targetBounds: Rect?) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            },
    ) {
        drawRect(color = Color.Black.copy(alpha = 0.68f))
        targetBounds?.let { rect ->
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(rect.left - 8.dp.toPx(), rect.top - 8.dp.toPx()),
                size = Size(rect.width + 16.dp.toPx(), rect.height + 16.dp.toPx()),
                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                blendMode = BlendMode.Clear,
            )
        }
    }
}

@Composable
private fun TargetGlow(targetBounds: Rect) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val topLeft = Offset(targetBounds.left - 8.dp.toPx(), targetBounds.top - 8.dp.toPx())
        val size = Size(targetBounds.width + 16.dp.toPx(), targetBounds.height + 16.dp.toPx())
        val cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
        val glowColor = Color.White

        drawRoundRect(
            color = glowColor.copy(alpha = 0.18f),
            topLeft = topLeft,
            size = size,
            cornerRadius = cornerRadius,
            style = Stroke(width = 16.dp.toPx()),
        )
        drawRoundRect(
            color = glowColor.copy(alpha = 0.34f),
            topLeft = topLeft,
            size = size,
            cornerRadius = cornerRadius,
            style = Stroke(width = 9.dp.toPx()),
        )
        drawRoundRect(
            color = glowColor,
            topLeft = topLeft,
            size = size,
            cornerRadius = cornerRadius,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
private fun EducationCard(
    activeEducation: ActiveEducation,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = activeEducation.step
    val cardColor = educationCardColor()
    val contentColor = if (cardColor.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    Surface(
        modifier = modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            },
        color = cardColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            step.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            step.illustration?.let { illustration ->
                EducationIllustrationView(illustration = illustration)
            }

            Text(
                text = step.text,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (step.bullets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    step.bullets.forEach { bullet ->
                        Text(
                            text = "• $bullet",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            EducationButtons(
                activeEducation = activeEducation,
                onNext = onNext,
                onBack = onBack,
                onFinish = onFinish,
            )
        }
    }
}

@Composable
private fun educationCardColor(): Color = if (isSystemInDarkTheme()) {
    Color(0xFF242424)
} else {
    MaterialTheme.colorScheme.surface
}

@Composable
private fun EducationButtons(
    activeEducation: ActiveEducation,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (activeEducation.canGoBack) {
            OutlinedButton(onClick = onBack) {
                Text(text = stringResource(StringsR.string.education_back))
            }
        }

        Button(
            onClick = if (activeEducation.isLastStep) onFinish else onNext,
        ) {
            Text(
                text = when {
                    activeEducation.education.steps.size == 1 -> stringResource(StringsR.string.education_got_it)
                    activeEducation.isLastStep -> stringResource(StringsR.string.education_done)
                    else -> stringResource(StringsR.string.education_next)
                },
            )
        }
    }
}

@Composable
private fun EducationIllustrationView(illustration: EducationIllustration) {
    when (illustration) {
        EducationIllustration.PullToRefresh -> PullToRefreshIllustration()
    }
}

@Composable
private fun PullToRefreshIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_swipe_down),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun calculateBubbleOffset(
    targetBounds: Rect?,
    maxWidthPx: Float,
    maxHeightPx: Float,
    bubbleWidthPx: Float,
    bubbleHeightPx: Float,
): Offset {
    if (targetBounds == null) {
        return Offset.Zero
    }

    val margin = 16f
    val x = min(
        max(margin, targetBounds.center.x - bubbleWidthPx / 2f),
        max(margin, maxWidthPx - bubbleWidthPx - margin),
    )
    val belowY = targetBounds.bottom + margin
    val y = if (belowY + bubbleHeightPx < maxHeightPx) {
        belowY
    } else {
        max(margin, targetBounds.top - bubbleHeightPx - margin)
    }

    return Offset(x, y)
}
