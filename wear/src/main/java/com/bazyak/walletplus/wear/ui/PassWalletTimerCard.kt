package com.bazyak.walletplus.wear.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.bazyak.walletplus.wear.R

/**
 * Proportions are expressed as fractions of the display so they hold on any watch size, measured
 * against a reference payment app on a 233dp round display.
 */
private const val ICON_WIDTH_FRACTION = 0.58f
private const val TIMER_BOTTOM_FRACTION = 0.08f
private val TimerFontSize = 16.sp
private val WalletPictogramColor = Color.White

/**
 * Timer page shown above the barcode.
 *
 * Counts down while it is the settled page and closes the app when it reaches zero, so a pass left
 * open on the wrist does not stay up indefinitely. Double-tapping anywhere buys more time.
 */
@Composable
internal fun PassWalletTimerCard(
    secondsRemaining: Int,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            },
        contentAlignment = Alignment.Center,
    ) {
        val iconSize = maxWidth * ICON_WIDTH_FRACTION
        val timerBottomPadding = maxHeight * TIMER_BOTTOM_FRACTION

        Image(
            painter = painterResource(R.drawable.ic_wallet_cards),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
        )

        Text(
            text = secondsRemaining.coerceAtLeast(0).toString(),
            color = WalletPictogramColor,
            fontSize = TimerFontSize,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = timerBottomPadding),
        )
    }
}
