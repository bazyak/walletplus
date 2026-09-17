package com.bazyak.walletplus.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bazyak.walletplus.corestrings.R as AppR
import com.bazyak.walletplus.data.archive.WalletArchive
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.PassCategory
import com.bazyak.walletplus.data.model.RefreshStatus
import com.bazyak.walletplus.designsystem.R as DesignR
import com.bazyak.walletplus.designsystem.components.branding.AppLogo
import com.bazyak.walletplus.designsystem.components.button.WalletIconButton
import com.bazyak.walletplus.designsystem.components.feedback.SnackbarStatus
import com.bazyak.walletplus.designsystem.components.feedback.WalletCircularProgressIndicator
import com.bazyak.walletplus.designsystem.components.feedback.WalletSnackbar
import com.bazyak.walletplus.designsystem.components.menu.WalletDropdownMenu
import com.bazyak.walletplus.designsystem.components.menu.WalletMenuItem
import com.bazyak.walletplus.designsystem.components.navigation.WalletTopAppBar
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.education.PassGridEducationTarget
import com.bazyak.walletplus.educations.EducationHost
import com.bazyak.walletplus.educations.EducationTargetProvider
import com.bazyak.walletplus.educations.educationTarget
import com.bazyak.walletplus.ui.components.DeleteZone
import com.bazyak.walletplus.ui.components.PassCardExpansion
import com.bazyak.walletplus.ui.components.PassGridSkeleton
import com.bazyak.walletplus.ui.components.RefreshLoadingSnackbar
import com.bazyak.walletplus.ui.components.pass.PassTile
import com.bazyak.walletplus.ui.components.pass.pkpass.ticket.TicketGridTile
import com.bazyak.walletplus.ui.navigation.Routes
import com.bazyak.walletplus.ui.viewmodel.EducationViewModel
import com.bazyak.walletplus.ui.viewmodel.ImportStatus
import com.bazyak.walletplus.ui.viewmodel.PassGridViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

/**
 * Grid screen displaying all passes.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "LongParameterList")
@Composable
fun PassGridScreen(
    viewModel: PassGridViewModel,
    educationViewModel: EducationViewModel,
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPreviewPass: (android.net.Uri) -> Unit,
    openPassId: String?,
    maximizeBrightnessOnPassOpen: Boolean,
    onOpenPassHandled: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passes by viewModel.passes.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val refreshStatus by viewModel.refreshStatus.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()
    val activeEducation by educationViewModel.activeEducation.collectAsState()
    val context = LocalContext.current
    var selectedPassId by remember { mutableStateOf<String?>(null) }
    var tilePositionCache by remember { mutableStateOf<IntRect?>(null) }
    var hideTileId by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current

    // Local state for optimistic UI updates during drag
    var localPasses by remember { mutableStateOf<List<Pass>>(emptyList()) }

    // Sync local state with ViewModel
    LaunchedEffect(passes) {
        localPasses = passes
    }

    LaunchedEffect(openPassId, localPasses, isInitialLoading) {
        val passId = openPassId ?: return@LaunchedEffect
        if (isInitialLoading) return@LaunchedEffect

        if (localPasses.any { pass -> pass.id == passId }) {
            selectedPassId = passId
            tilePositionCache = null
            onOpenPassHandled()
        } else if (localPasses.isNotEmpty()) {
            onOpenPassHandled()
        }
    }

    LaunchedEffect(isInitialLoading, localPasses.size, activeEducation) {
        educationViewModel.showPassGridEducationIfNeeded(
            passCount = localPasses.size,
            isInitialLoading = isInitialLoading,
        )
    }

    // Lazy grid state for reorderable
    val gridState = rememberLazyGridState()

    // Delete zone state
    var deleteZoneTop by remember { mutableFloatStateOf(0f) }
    var deleteZoneBottom by remember { mutableFloatStateOf(0f) }
    var deleteZoneLeft by remember { mutableFloatStateOf(0f) }
    var deleteZoneRight by remember { mutableFloatStateOf(0f) }
    var fingerPositionY by remember { mutableFloatStateOf(0f) }
    var fingerPositionX by remember { mutableFloatStateOf(0f) }
    var passToDelete by remember { mutableStateOf<Pass?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartedPasses by remember { mutableStateOf<List<Pass>>(emptyList()) }

    // Check if finger position is over delete zone
    val isOverDeleteZone = isDragging &&
        fingerPositionY > deleteZoneTop &&
        fingerPositionY < deleteZoneBottom &&
        fingerPositionX > deleteZoneLeft &&
        fingerPositionX < deleteZoneRight

    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()

    // Reorderable state with move callback
    val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
        // Only allow reordering when not importing and not over delete zone
        if (importStatus !is ImportStatus.Loading && !isOverDeleteZone) {
            localPasses = localPasses.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    }

    // Add method dropdown menu state
    var showAddMenu by remember { mutableStateOf(false) }

    // File picker launcher for pass preview. Keeps ZIP detection for broad file managers.
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            if (WalletArchive.isWalletArchiveUri(context, it)) {
                viewModel.importWalletArchive(it)
            } else {
                onPreviewPass(it)
                navController.navigate(Routes.PREVIEW)
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            navController.navigate(Routes.CAMERA_SCAN)
        }
    }

    EducationTargetProvider {
        Box(modifier = modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing =
                refreshStatus is RefreshStatus.Loading && (refreshStatus as? RefreshStatus.Loading)?.passId == null,
                onRefresh = { viewModel.refreshAllPasses() },
                modifier = Modifier.fillMaxSize(),
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = refreshStatus is RefreshStatus.Loading &&
                            (refreshStatus as? RefreshStatus.Loading)?.passId == null,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
            ) {
                Scaffold(
                    topBar = {
                        WalletTopAppBar(
                            title = {
                                AppLogo()
                            },
                            actions = {
                                WalletIconButton(
                                    onClick = onSettingsClick,
                                ) {
                                    Icon(
                                        painter = painterResource(id = DesignR.drawable.settings),
                                        contentDescription = stringResource(AppR.string.settings),
                                    )
                                }
                                AddPassActions(
                                    showAddMenu = showAddMenu,
                                    onShowAddMenuChange = { showAddMenu = it },
                                    onPickFile = { pickFileLauncher.launch("*/*") },
                                    onRequestCamera = {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                )
                            },
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding())
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        // Track finger position without consuming events
                                        event.changes.firstOrNull()?.let { change ->
                                            fingerPositionX = change.position.x
                                            fingerPositionY =
                                                change.position.y + paddingValues.calculateTopPadding().toPx()
                                        }
                                    }
                                }
                            },
                    ) {
                        if (isInitialLoading) {
                            // Loading skeleton while fetching initial data
                            PassGridSkeleton()
                        } else if (localPasses.isEmpty()) {
                            // Empty state
                            EmptyPassGridState()
                        } else {
                            // Grid of passes
                            PassGridContent(
                                localPasses = localPasses,
                                gridState = gridState,
                                reorderableState = reorderableState,
                                paddingValues = paddingValues,
                                hideTileId = hideTileId,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                haptic = haptic,
                                isDragging = isDragging,
                                passToDelete = passToDelete,
                                isOverDeleteZone = isOverDeleteZone,
                                dragStartedPasses = dragStartedPasses,
                                onDragStart = { pass ->
                                    isDragging = true
                                    passToDelete = pass
                                    dragStartedPasses = localPasses
                                },
                                onDragEnd = { pass, shouldDelete ->
                                    if (isDragging && passToDelete?.id == pass.id) {
                                        isDragging = false
                                        if (shouldDelete && passToDelete != null) {
                                            viewModel.deletePass(passToDelete!!)
                                            localPasses = localPasses.filter { it.id != passToDelete!!.id }
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } else if (localPasses != dragStartedPasses) {
                                            viewModel.updatePassOrder(
                                                localPasses.mapIndexed { index, p ->
                                                    p.id to index
                                                }.toMap(),
                                            )
                                        }
                                        passToDelete = null
                                        fingerPositionY = 0f
                                        fingerPositionX = 0f
                                        dragStartedPasses = emptyList()
                                    }
                                },
                                onTileClick = { pass -> selectedPassId = pass.id },
                                onTilePositioned = { rect -> tilePositionCache = rect },
                            )
                        }

                        // Delete zone at bottom (only visible during drag)
                        DeleteZone(
                            isVisible = isDragging,
                            isHovering = isOverDeleteZone,
                            onPositioned = { left, top, right, bottom ->
                                deleteZoneLeft = left
                                deleteZoneTop = top
                                deleteZoneRight = right
                                deleteZoneBottom = bottom
                            },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )

                        // Import status snackbar
                        if (importStatus is ImportStatus.Summary) {
                            WalletSnackbar(
                                message = (importStatus as ImportStatus.Summary).message,
                                status = SnackbarStatus.SUCCESS,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(MaterialTheme.spacing.mediumLarge),
                            )
                        }

                        if (importStatus is ImportStatus.Error) {
                            WalletSnackbar(
                                message = (importStatus as ImportStatus.Error).message,
                                status = SnackbarStatus.ERROR,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(MaterialTheme.spacing.mediumLarge),
                            )
                        }

                        if (importStatus is ImportStatus.Loading && !isInitialLoading) {
                            WalletCircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                }
            }

            // Show expansion overlay if a pass is selected (rendered on top of Scaffold)
            selectedPassId?.let { passId ->
                PassCardExpansion(
                    passId = passId,
                    tilePosition = tilePositionCache,
                    viewModel = viewModel,
                    maximizeBrightnessOnFrontSide = maximizeBrightnessOnPassOpen,
                    onTileVisibilityChange = { visible ->
                        // Animation controls tile visibility timing
                        hideTileId = if (visible) null else passId
                    },
                    onDismiss = {
                        selectedPassId = null
                        tilePositionCache = null
                    },
                )
            }

            // Refresh status snackbar (rendered on top of everything)
            RefreshLoadingSnackbar(
                refreshStatus = refreshStatus,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = if (importStatus is ImportStatus.Error) 80.dp else MaterialTheme.spacing.mediumLarge,
                    ),
            )

            EducationHost(
                activeEducation = activeEducation,
                onNext = educationViewModel::nextEducationStep,
                onBack = educationViewModel::previousEducationStep,
                onFinish = educationViewModel::finishActiveEducation,
            )
        }
    }
}

@Composable
private fun AddPassActions(
    showAddMenu: Boolean,
    onShowAddMenuChange: (Boolean) -> Unit,
    onPickFile: () -> Unit,
    onRequestCamera: () -> Unit,
) {
    Box {
        WalletIconButton(
            onClick = { onShowAddMenuChange(true) },
            modifier = Modifier.educationTarget(PassGridEducationTarget.ADD_BUTTON),
        ) {
            Icon(
                painter = painterResource(id = DesignR.drawable.plus),
                contentDescription = stringResource(AppR.string.add_pass),
            )
        }
        WalletDropdownMenu(
            expanded = showAddMenu,
            onDismissRequest = { onShowAddMenuChange(false) },
        ) {
            WalletMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = DesignR.drawable.file),
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(AppR.string.add_from_files)) },
                onClick = {
                    onShowAddMenuChange(false)
                    onPickFile()
                },
            )
            WalletMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = DesignR.drawable.camera),
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(AppR.string.add_from_camera)) },
                onClick = {
                    onShowAddMenuChange(false)
                    onRequestCamera()
                },
            )
        }
    }
}

/**
 * Empty state shown when there are no passes.
 */
@Composable
private fun EmptyPassGridState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(AppR.string.no_passes_yet),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Text(
            text = stringResource(AppR.string.tap_to_add_pass),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Grid content showing the list of passes with reordering support.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("LongParameterList")
private fun PassGridContent(
    localPasses: List<Pass>,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    reorderableState: sh.calvin.reorderable.ReorderableLazyGridState,
    paddingValues: PaddingValues,
    hideTileId: String?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    isDragging: Boolean,
    passToDelete: Pass?,
    isOverDeleteZone: Boolean,
    dragStartedPasses: List<Pass>,
    onDragStart: (Pass) -> Unit,
    onDragEnd: (Pass, Boolean) -> Unit,
    onTileClick: (Pass) -> Unit,
    onTilePositioned: (IntRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.mediumLarge,
            end = MaterialTheme.spacing.mediumLarge,
            top = MaterialTheme.spacing.mediumLarge,
            bottom = MaterialTheme.spacing.mediumLarge + paddingValues.calculateBottomPadding(),
        ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.mediumLarge),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.mediumLarge),
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(localPasses, key = { _, pass -> pass.id }) { index, pass ->
            ReorderableItem(reorderableState, key = pass.id) { itemIsDragging ->
                // Track when any item starts/stops dragging
                LaunchedEffect(itemIsDragging) {
                    if (itemIsDragging) {
                        onDragStart(pass)
                    } else if (isDragging && passToDelete?.id == pass.id) {
                        // This item just stopped dragging (finger lifted)
                        onDragEnd(pass, isOverDeleteZone)
                    }
                }

                if (pass.category == PassCategory.EVENT_TICKET) {
                    TicketGridTile(
                        pass = pass,
                        isDragging = itemIsDragging,
                        isExpanded = hideTileId == pass.id,
                        modifier = Modifier
                            .then(firstCardEducationModifier(index))
                            .longPressDraggableHandle(
                                onDragStarted = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragStopped = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            ),
                        onClick = { onTileClick(pass) },
                        onPositioned = { rect -> onTilePositioned(rect) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                } else {
                    PassTile(
                        pass = pass,
                        isDragging = itemIsDragging,
                        isExpanded = hideTileId == pass.id,
                        modifier = Modifier
                            .then(firstCardEducationModifier(index))
                            .longPressDraggableHandle(
                                onDragStarted = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragStopped = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            ),
                        onClick = { onTileClick(pass) },
                        onPositioned = { rect -> onTilePositioned(rect) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
        }
    }
}

private fun firstCardEducationModifier(index: Int): Modifier = if (index == 0) {
    Modifier.educationTarget(PassGridEducationTarget.FIRST_PASS_CARD)
} else {
    Modifier
}
