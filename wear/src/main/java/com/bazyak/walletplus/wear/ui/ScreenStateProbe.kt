package com.bazyak.walletplus.wear.ui

import android.app.KeyguardManager
import android.hardware.display.DisplayManager
import android.os.PowerManager
import android.util.Log
import android.view.Display
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

private const val PROBE_TAG = "ScreenStateProbe"
private const val PROBE_INTERVAL_MS = 300L

/**
 * Diagnostic only. Logs the display and lock state while a screen is held open, so a wrist-turn
 * can be correlated with what the system actually does to the display.
 *
 * NFC (including HCE routed to another app) needs more than a visibly lit screen: the device also
 * has to be interactive and unlocked. Logging all four values together tells which of those the
 * wrist-turn breaks. Remove before release.
 */
@Composable
internal fun ScreenStateProbe(isEnabled: Boolean) {
    val context = LocalContext.current

    LaunchedEffect(isEnabled) {
        if (!isEnabled) return@LaunchedEffect

        val powerManager = context.getSystemService(PowerManager::class.java)
        val keyguardManager = context.getSystemService(KeyguardManager::class.java)
        val displayManager = context.getSystemService(DisplayManager::class.java)

        var previous: String? = null
        while (true) {
            val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            val line = "state=${display?.state.displayStateName()} " +
                "interactive=${powerManager?.isInteractive} " +
                "deviceLocked=${keyguardManager?.isDeviceLocked} " +
                "keyguardLocked=${keyguardManager?.isKeyguardLocked}"

            // Only log transitions: a steady 3/s stream buries the moment the wrist turns.
            if (line != previous) {
                previous = line
                Log.i(PROBE_TAG, line)
            }

            delay(PROBE_INTERVAL_MS)
        }
    }
}

private fun Int?.displayStateName(): String = when (this) {
    Display.STATE_OFF -> "OFF($this)"
    Display.STATE_ON -> "ON($this)"
    Display.STATE_DOZE -> "DOZE($this)"
    Display.STATE_DOZE_SUSPEND -> "DOZE_SUSPEND($this)"
    Display.STATE_ON_SUSPEND -> "ON_SUSPEND($this)"
    null -> "null"
    else -> "UNKNOWN($this)"
}
