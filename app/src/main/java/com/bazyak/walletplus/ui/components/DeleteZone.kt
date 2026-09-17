package com.bazyak.walletplus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.corestrings.R as AppR
import com.bazyak.walletplus.designsystem.R as DesignR
import com.bazyak.walletplus.designsystem.foundation.color.ColorTokens
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing

@Composable
fun DeleteZone(
    isVisible: Boolean,
    isHovering: Boolean,
    onPositioned: (left: Float, top: Float, right: Float, bottom: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier,
    ) {
        val backgroundColor = if (isHovering) {
            ColorTokens.deleteZoneHoverBackground
        } else {
            ColorTokens.deleteZoneIdleBackground
        }
        val contentColor = ColorTokens.deleteZoneContent

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.huge, vertical = MaterialTheme.spacing.extraLarge)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    val size = coordinates.size
                    onPositioned(
                        position.x,
                        position.y,
                        position.x + size.width,
                        position.y + size.height,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge),
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.delete),
                        contentDescription = stringResource(AppR.string.delete),
                        tint = contentColor,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                    Text(
                        text = stringResource(AppR.string.drag_here_to_delete),
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
