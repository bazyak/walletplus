package com.bazyak.walletplus.ui.components.pass

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassData
import com.bazyak.walletplus.data.model.getPassData
import com.bazyak.walletplus.designsystem.foundation.color.createCustomPassGradient
import com.bazyak.walletplus.designsystem.foundation.color.ensureContrast
import com.bazyak.walletplus.designsystem.foundation.color.parseColor
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.ui.utils.IconMapper
import com.bazyak.walletplus.ui.utils.stripHtml
import java.io.File

/**
 * Individual pass tile in the grid.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PassTile(
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
    val foregroundColor = pass.foregroundColor?.let { parseColor(it) }
        ?: MaterialTheme.colorScheme.onSurface

    val textColor = ensureContrast(
        foregroundColor = pass.foregroundColor?.let { parseColor(it) },
        backgroundColor = backgroundColor,
        isDarkTheme = isDarkTheme,
        lightFallback = MaterialTheme.colorScheme.onSurface,
        darkFallback = MaterialTheme.colorScheme.onSurface,
    )

    // Track this tile's position
    var currentPosition by remember { mutableStateOf<IntRect?>(null) }

    val passData = remember(pass) { pass.getPassData() }

    with(sharedTransitionScope) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(0.70f) // Vertical rectangle (card-like proportions)
                .scale(if (isDragging) 1.05f else 1f)
                .graphicsLayer {
                    // Hide entire card when expanded to create placeholder effect
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
            colors = CardDefaults.cardColors(
                containerColor = if (passData is PassData.Custom) Color.Transparent else backgroundColor,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDragging) {
                    MaterialTheme.spacing.mediumLarge
                } else {
                    MaterialTheme.spacing.extraSmall
                },
            ),
            shape = RoundedCornerShape(12.dp),
            onClick = {
                // Pass the current position when clicked
                currentPosition?.let { onPositioned(it) }
                onClick()
            },
        ) {
            // Gradient only for custom passes; PKPass tiles use their issuer-defined solid colour
            val gradient = if (passData is PassData.Custom) {
                createCustomPassGradient(backgroundColor, foregroundColor)
            } else {
                null
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (gradient != null) {
                            Modifier.background(gradient, RoundedCornerShape(12.dp))
                        } else {
                            Modifier
                        },
                    )
                    .padding(MaterialTheme.spacing.mediumLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Spacer to push content down
                Spacer(modifier = Modifier.weight(1f))

                // Logo/Icon - check if custom pass or PKPass
                when (passData) {
                    is PassData.Custom -> {
                        // Show custom icon for custom passes
                        val iconRes = IconMapper.getIconByName(passData.customPassJson.iconName)
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "icon-${pass.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ),
                            tint = textColor,
                        )
                    }

                    is PassData.PKPass -> {
                        // Show logo/icon from file for PKPass
                        val logoPath = pass.logoPath ?: pass.iconPath
                        val logoFile = File(logoPath)
                        if (logoFile.exists()) {
                            val bitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(60.dp)
                                        .widthIn(max = 120.dp)
                                        .sharedElement(
                                            rememberSharedContentState(key = "icon-${pass.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        ),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                    }
                }

                // Spacer to push text to bottom
                Spacer(modifier = Modifier.weight(1f))

                // Organization name with HTML stripped - at bottom
                Text(
                    text = stripHtml(pass.organizationName),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.W400,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
