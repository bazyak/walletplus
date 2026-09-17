package com.bazyak.walletplus.wear.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import com.bazyak.walletplus.wear.data.CachedWearPass
import com.bazyak.walletplus.wear.data.WearPassRepository

@Composable
fun WearWalletApp(
    repository: WearPassRepository,
    onOpenPassOnPhone: (String) -> Unit,
    onExitApp: () -> Unit,
) {
    MaterialTheme {
        var passes by remember { mutableStateOf<List<CachedWearPass>?>(null) }
        var selectedPassId by rememberSaveable { mutableStateOf<String?>(null) }
        val passListState = rememberScalingLazyListState(initialCenterItemIndex = 0)
        val haptic = LocalHapticFeedback.current
        val loadedPasses = passes
        val selectedPass = selectedPassId?.let { passId ->
            loadedPasses?.firstOrNull { it.snapshot.id == passId }
        }

        LaunchedEffect(repository) {
            repository.observePasses().collect { loaded ->
                passes = loaded
            }
        }

        LaunchedEffect(selectedPassId, loadedPasses) {
            if (
                selectedPassId != null &&
                loadedPasses != null &&
                loadedPasses.none { it.snapshot.id == selectedPassId }
            ) {
                selectedPassId = null
            }
        }

        BackHandler(enabled = selectedPass != null) {
            haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
            selectedPassId = null
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WearBackground),
        ) {
            AnimatedContent(
                targetState = selectedPass,
                transitionSpec = {
                    val direction = if (targetState != null) 1 else -1
                    slideInHorizontally { fullWidth -> direction * fullWidth }.togetherWith(
                        slideOutHorizontally { fullWidth -> -direction * fullWidth / 3 },
                    ).using(
                        SizeTransform(clip = false),
                    )
                },
                label = "wear-screen-transition",
            ) { pass ->
                if (pass != null) {
                    WearPassScreen(
                        pass = pass,
                        onOpenPassOnPhone = onOpenPassOnPhone,
                        onExitApp = onExitApp,
                    )
                } else {
                    val currentPasses = passes
                    if (currentPasses != null) {
                        WearListScreen(
                            passes = currentPasses,
                            listState = passListState,
                            onPassClick = { selected ->
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                selectedPassId = selected.snapshot.id
                            },
                        )
                    }
                }
            }
        }
    }
}
