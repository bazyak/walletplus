package com.bazyak.walletplus.wear.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ScreenScaffold
import com.bazyak.walletplus.wear.data.CachedWearPass
import kotlinx.coroutines.delay
import kotlin.math.abs

private const val WALLET_PAGE_INDEX = 0
private const val QR_PAGE_INDEX = 1
private const val HEADER_PAGE_INDEX = 2
private const val PASS_PAGE_COUNT = 3
private const val TIMER_START_SECONDS = 15
private const val TIMER_BONUS_SECONDS = 5
private const val TIMER_TICK_MS = 1000L
private val PassPageSpacing = 24.dp

@Composable
internal fun WearPassScreen(
    pass: CachedWearPass,
    onOpenPassOnPhone: (String) -> Unit,
    onExitApp: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = QR_PAGE_INDEX, pageCount = { PASS_PAGE_COUNT })
    val headerScrollState = rememberScrollState()
    val view = LocalView.current
    val pagePosition = pagerState.pagePosition()
    val isHeaderPage = pagerState.isPageSettledAt(HEADER_PAGE_INDEX)
    val isWalletPage = pagerState.isPageSettledAt(WALLET_PAGE_INDEX)
    var secondsRemaining by remember { mutableIntStateOf(TIMER_START_SECONDS) }
    val headerColor = remember(pass.snapshot.backgroundColor) {
        parseWearColor(pass.snapshot.backgroundColor, Color(0xFF0077B6))
    }
    val headerIndicatorColor = remember(pass.snapshot.foregroundColor, headerColor) {
        readableColor(
            foreground = parseWearColor(pass.snapshot.foregroundColor, Color.White),
            background = headerColor,
        )
    }

    KeepScreenBrightness(isEnabled = isWalletPage, brightness = 0.8f)
    KeepScreenOn(isEnabled = isWalletPage)
    ScreenStateProbe(isEnabled = isWalletPage)

    // The countdown only runs while the wallet page is settled. Leaving the page resets it, so
    // coming back always starts from a full interval rather than resuming a stale one.
    LaunchedEffect(isWalletPage) {
        if (!isWalletPage) {
            secondsRemaining = TIMER_START_SECONDS
            return@LaunchedEffect
        }

        while (secondsRemaining > 0) {
            delay(TIMER_TICK_MS)
            secondsRemaining--
        }

        onExitApp()
    }

    LaunchedEffect(pagerState, view) {
        var previousPage = pagerState.currentPage
        snapshotFlow { pagerState.settledPassPage() }.collect { page ->
            if (page != null && page != previousPage) {
                previousPage = page
                view.performWearScrollTickHaptic()
            }
        }
    }

    ScreenScaffold(
        scrollState = headerScrollState,
        timeText = null,
        scrollIndicator = null,
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBackground),
        contentPadding = PaddingValues(),
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            VerticalPager(
                state = pagerState,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    pagerSnapDistance = PagerSnapDistance.atMost(1),
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(
                        rememberWearPassHeaderTouchScrollConnection(
                            pagerState = pagerState,
                            headerScrollState = headerScrollState,
                            headerPageIndex = HEADER_PAGE_INDEX,
                        ),
                    ),
                contentPadding = contentPadding,
                pageSpacing = PassPageSpacing,
            ) { page ->
                when (page) {
                    WALLET_PAGE_INDEX -> ScaledPagerPage(pagerState = pagerState, page = page) {
                        PassWalletTimerCard(
                            secondsRemaining = secondsRemaining,
                            onDoubleTap = { secondsRemaining += TIMER_BONUS_SECONDS },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    QR_PAGE_INDEX -> ScaledPagerPage(pagerState = pagerState, page = page) {
                        PassQrCard(
                            pass = pass,
                            scrollHintProgress = (pagePosition * 2f).coerceIn(0f, 1f),
                            showScrollHint = true,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    HEADER_PAGE_INDEX -> ScaledPagerPage(pagerState = pagerState, page = page) {
                        PassHeaderCard(
                            pass = pass,
                            scrollState = headerScrollState,
                            isRotaryEnabled = isHeaderPage,
                            onOpenPassOnPhone = { onOpenPassOnPhone(pass.snapshot.id) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            PassCurvedScrollIndicator(
                pageProgress = pagePosition,
                headerScrollState = headerScrollState,
                headerColor = headerIndicatorColor,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ScaledPagerPage(
    pagerState: PagerState,
    page: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val distance = abs(pagerState.pageOffsetFrom(page)).coerceIn(0f, 1f)
    val scale by animateFloatAsState(
        targetValue = 1f - distance * 0.16f,
        label = "wear-pass-page-scale",
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale),
        ) {
            content()
        }
    }
}

private fun PagerState.pageOffsetFrom(page: Int): Float = (currentPage - page) + currentPageOffsetFraction

private fun PagerState.settledPassPage(): Int? = when {
    isPageSettledAt(WALLET_PAGE_INDEX) -> WALLET_PAGE_INDEX
    isPageSettledAt(QR_PAGE_INDEX) -> QR_PAGE_INDEX
    isPageSettledAt(HEADER_PAGE_INDEX) -> HEADER_PAGE_INDEX
    else -> null
}
