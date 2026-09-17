package com.bazyak.walletplus.ui.components.pass.pkpass.ticket

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.designsystem.foundation.color.ensureContrast
import com.bazyak.walletplus.designsystem.foundation.color.parseColor
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.ui.components.common.DottedDivider
import com.bazyak.walletplus.ui.utils.stripHtml
import java.io.File

/**
 * Grid tile for ticket-style passes.
 * Shows a scaled-down version of the ticket layout with TicketShape and two content sections.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TicketGridTile(
    pass: Pass,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onClick: () -> Unit,
    onPositioned: (IntRect) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = pass.backgroundColor?.let { parseColor(it) }
        ?: MaterialTheme.colorScheme.surfaceVariant
    val textColor = ensureContrast(
        foregroundColor = pass.foregroundColor?.let { parseColor(it) },
        backgroundColor = backgroundColor,
        isDarkTheme = isDarkTheme,
        lightFallback = MaterialTheme.colorScheme.onSurface,
        darkFallback = MaterialTheme.colorScheme.onSurface,
    )
    var currentPosition by remember { mutableStateOf<IntRect?>(null) }

    with(sharedTransitionScope) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(0.70f)
                .scale(if (isDragging) 1.05f else 1f)
                .shadow(
                    elevation = if (isDragging) {
                        MaterialTheme.spacing.mediumLarge
                    } else {
                        MaterialTheme.spacing.extraSmall
                    },
                    shape = TicketShape(
                        cornerRadius = 12.dp,
                        notchPosition = 0.6f,
                    ),
                    clip = false,
                )
                .graphicsLayer {
                    alpha = if (isExpanded) 0f else 1f
                }
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    val size = coordinates.size
                    currentPosition = IntRect(
                        left = position.x.toInt(),
                        top = position.y.toInt(),
                        right = (position.x + size.width).toInt(),
                        bottom = (position.y + size.height).toInt(),
                    )
                }
                .sharedElement(
                    rememberSharedContentState(key = "card-${pass.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            shape = TicketShape(
                cornerRadius = 12.dp,
                notchPosition = 0.6f,
            ),
            color = backgroundColor,
            onClick = {
                currentPosition?.let { onPositioned(it) }
                onClick()
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // TOP SECTION (60%) - Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                        .padding(MaterialTheme.spacing.small),
                    contentAlignment = Alignment.Center,
                ) {
                    val logoPath = pass.logoPath ?: pass.iconPath
                    val logoFile = File(logoPath)
                    if (logoFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(32.dp)
                                    .widthIn(max = 80.dp)
                                    .sharedElement(
                                        rememberSharedContentState(key = "icon-${pass.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    ),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }

                // DOTTED DIVIDER
                DottedDivider(dotRadius = 4f, modifier = Modifier.padding(horizontal = 12.dp))

                // BOTTOM SECTION (40%) - Organization name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .padding(MaterialTheme.spacing.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stripHtml(pass.organizationName),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
