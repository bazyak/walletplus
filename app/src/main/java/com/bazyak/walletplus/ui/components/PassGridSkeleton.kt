package com.bazyak.walletplus.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing

@Composable
fun PassGridSkeleton(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.mediumLarge,
            end = MaterialTheme.spacing.mediumLarge,
            top = MaterialTheme.spacing.mediumLarge,
            bottom = MaterialTheme.spacing.mediumLarge,
        ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.mediumLarge),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.mediumLarge),
        modifier = modifier.fillMaxSize(),
    ) {
        items(6) { index ->
            SkeletonCard(delayMillis = index * 120)
        }
    }
}

@Composable
private fun SkeletonCard(delayMillis: Int, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_$delayMillis")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMillis),
        ),
        label = "skeleton_alpha_$delayMillis",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.70f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.mediumLarge),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Skeleton for logo/icon
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Skeleton for text
            Box(
                modifier = Modifier
                    .height(MaterialTheme.spacing.mediumLarge)
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
            )
        }
    }
}
