package com.bazyak.walletplus.wear.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

private const val GALAXY_WATCH_SCROLL_TICK = 101
private const val WEAR_4_SCROLL_TICK = 18
private const val WEAR_3_5_SCROLL_TICK = 10002

internal fun View.performWearScrollTickHaptic() {
    performHapticFeedback(scrollTickHapticConstant())
}

private fun scrollTickHapticConstant(): Int {
    val fallbackTick = fallbackScrollTickHapticConstant()
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> wearSdkScrollTick() ?: fallbackTick
        else -> fallbackTick
    }
}

private fun fallbackScrollTickHapticConstant(): Int = when {
    isGalaxyWatch() -> GALAXY_WATCH_SCROLL_TICK
    Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU -> WEAR_4_SCROLL_TICK
    Build.VERSION.SDK_INT == Build.VERSION_CODES.R -> WEAR_3_5_SCROLL_TICK
    else -> HapticFeedbackConstants.CLOCK_TICK
}

private fun wearSdkScrollTick(): Int? = runCatching {
    Class.forName("com.google.wear.input.WearHapticFeedbackConstants")
        .getMethod("getScrollTick")
        .invoke(null) as? Int
}.getOrNull()

private fun isGalaxyWatch(): Boolean = Build.MANUFACTURER.contains("Samsung", ignoreCase = true) &&
    Build.MODEL.matches("^SM-R.*$".toRegex())
