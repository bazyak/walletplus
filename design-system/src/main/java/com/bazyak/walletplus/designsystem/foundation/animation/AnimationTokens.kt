package com.bazyak.walletplus.designsystem.foundation.animation

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

/**
 * Animation specifications and constants for the Wallet design system.
 *
 * Defines timing, easing, and animation specs for consistent motion across the app.
 * Primarily used for pass card expansion/dismissal animations.
 *
 * Animation phases:
 * - Phase 1: Initial fade-in at tile size (50ms)
 * - Phase 2: Scale up to full size with scrim and buttons (300ms)
 * - Dismissal: Scale down, then fade out
 * - Flip: Card flip animation (600ms)
 */
object AnimationTokens {
    // ========== Durations ==========

    /**
     * Phase 1: Fade-in duration at tile size (card appears scaled down).
     */
    const val PHASE_1_DURATION_MS = 50

    /**
     * Delay between animation phases.
     */
    const val INTER_PHASE_DELAY_MS = 16

    /**
     * Phase 2: Scale up to full size with scrim and buttons.
     */
    const val PHASE_2_DURATION_MS = 300

    /**
     * Total expansion animation duration.
     */
    const val TOTAL_EXPANSION_MS = PHASE_1_DURATION_MS + INTER_PHASE_DELAY_MS + PHASE_2_DURATION_MS

    /**
     * Dismissal Phase 1: Scale down to tile size.
     */
    const val DISMISSAL_PHASE_1_DURATION_MS = 250

    /**
     * Dismissal Phase 2: Fade out at tile size.
     */
    const val DISMISSAL_PHASE_2_DURATION_MS = 100

    /**
     * Total dismissal animation duration.
     */
    const val DISMISSAL_TOTAL_MS =
        DISMISSAL_PHASE_1_DURATION_MS + INTER_PHASE_DELAY_MS + DISMISSAL_PHASE_2_DURATION_MS

    /**
     * Card flip animation duration.
     */
    const val FLIP_DURATION_MS = 600

    /**
     * Swipe gesture threshold in dp.
     */
    const val SWIPE_THRESHOLD_DP = 50

    // ========== Animation Specs ==========

    /**
     * Phase 1: Card fade-in animation spec (linear easing).
     */
    val phaseOneCardFadeIn = tween<Float>(
        durationMillis = PHASE_1_DURATION_MS,
        easing = LinearEasing,
    )

    /**
     * Phase 2: Scale animation spec (fast out, slow in).
     */
    val phaseTwoScale = tween<Float>(
        durationMillis = PHASE_2_DURATION_MS,
        easing = FastOutSlowInEasing,
    )

    /**
     * Phase 2: Scrim fade-in animation spec (linear easing).
     */
    val phaseTwoScrim = tween<Float>(
        durationMillis = PHASE_2_DURATION_MS,
        easing = LinearEasing,
    )

    /**
     * Phase 2: Button appearance animation spec (ease out cubic).
     */
    val phaseTwoButtons = tween<Float>(
        durationMillis = PHASE_2_DURATION_MS,
        easing = EaseOutCubic,
    )

    /**
     * Dismissal Phase 1: Scale down animation spec (fast out, slow in).
     */
    val dismissalPhaseOneScale = tween<Float>(
        durationMillis = DISMISSAL_PHASE_1_DURATION_MS,
        easing = FastOutSlowInEasing,
    )

    /**
     * Dismissal Phase 2: Fade out animation spec (linear easing).
     */
    val dismissalPhaseTwoFade = tween<Float>(
        durationMillis = DISMISSAL_PHASE_2_DURATION_MS,
        easing = LinearEasing,
    )
}
