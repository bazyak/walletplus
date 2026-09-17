package com.bazyak.walletplus.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onPreRotaryScrollEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.bazyak.walletplus.corestrings.R
import com.bazyak.walletplus.wear.data.CachedWearPass

private val PassListCardHeight = 94.dp

@Composable
internal fun WearListScreen(
    passes: List<CachedWearPass>,
    listState: ScalingLazyListState,
    onPassClick: (CachedWearPass) -> Unit,
) {
    if (passes.isEmpty()) {
        EmptyPassState()
        return
    }

    val view = LocalView.current
    val flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState)
    val rotaryBehavior = RotaryScrollableDefaults.snapBehavior(
        scrollableState = listState,
        hapticFeedbackEnabled = false,
    )

    ScreenScaffold(
        scrollState = listState,
        timeText = null,
        scrollIndicator = null,
        modifier = Modifier
            .fillMaxSize()
            .background(WearBackground),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                rotaryScrollableBehavior = rotaryBehavior,
                modifier = Modifier
                    .fillMaxSize()
                    .background(WearBackground)
                    .onPreRotaryScrollEvent { event ->
                        val delta = event.verticalScrollPixels.takeIf { it != 0f } ?: event.horizontalScrollPixels
                        val canScroll = when {
                            delta > 0f -> listState.canScrollForward
                            delta < 0f -> listState.canScrollBackward
                            else -> false
                        }
                        if (canScroll) {
                            view.performWearScrollTickHaptic()
                        }
                        false
                    },
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                autoCentering = AutoCenteringParams(itemIndex = 0),
            ) {
                itemsIndexed(
                    items = passes,
                    key = { _, pass -> pass.snapshot.id },
                ) { _, pass ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        PassListItem(
                            pass = pass,
                            onClick = { onPassClick(pass) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            ListCurvedScrollIndicator(
                listState = listState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun EmptyPassState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.no_passes),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.add_or_manage_passes_on_phone),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PassListItem(pass: CachedWearPass, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cardColor = remember(pass.snapshot.backgroundColor) {
        parseWearColor(pass.snapshot.backgroundColor, Color(0xFFFFFFFF))
    }
    val textColor = remember(pass.snapshot.foregroundColor, cardColor) {
        readableColor(
            foreground = parseWearColor(pass.snapshot.foregroundColor, Color.Black),
            background = cardColor,
        )
    }

    Row(
        modifier = modifier
            .height(PassListCardHeight)
            .clip(RoundedCornerShape(28.dp))
            .background(cardColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PassIcon(pass = pass, tint = textColor, background = cardColor, size = 48.dp)
        Column(
            modifier = Modifier
                .padding(start = 14.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = pass.snapshot.title,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
