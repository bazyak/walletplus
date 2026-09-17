package com.bazyak.walletplus.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.mlkit.vision.barcode.common.Barcode
import com.bazyak.walletplus.camera.CameraScanScreen
import com.bazyak.walletplus.camera.ScanResult
import com.bazyak.walletplus.corestrings.R as AppR
import com.bazyak.walletplus.designsystem.components.branding.AppLogo
import com.bazyak.walletplus.educations.OnboardingScreen
import com.bazyak.walletplus.ui.screens.CustomPassBuilderScreen
import com.bazyak.walletplus.ui.screens.InitialScreen
import com.bazyak.walletplus.ui.screens.InitialScreenActions
import com.bazyak.walletplus.ui.screens.PassGridScreen
import com.bazyak.walletplus.ui.screens.PassPreviewScreen
import com.bazyak.walletplus.ui.screens.SettingsScreen
import com.bazyak.walletplus.ui.viewmodel.EducationViewModel
import com.bazyak.walletplus.ui.viewmodel.PassGridViewModel
import com.bazyak.walletplus.ui.viewmodel.PassPreviewViewModel

/**
 * Navigation routes for the app.
 */
object Routes {
    const val INITIAL = "initial"
    const val ONBOARDING = "onboarding"
    const val GRID = "grid"
    const val PREVIEW = "preview"
    const val CAMERA_SCAN = "camera_scan"
    const val SETTINGS = "settings"
    const val CUSTOM_PASS_BUILDER = "custom_pass_builder/{barcodeValue}/{barcodeFormat}"

    fun customPassBuilder(barcodeValue: String, barcodeFormat: String) =
        "custom_pass_builder/$barcodeValue/$barcodeFormat"
}

/**
 * Main navigation graph with shared element transitions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("LongParameterList")
@Composable
fun PassNavGraph(
    navController: NavHostController,
    gridViewModel: PassGridViewModel,
    previewViewModel: PassPreviewViewModel,
    educationViewModel: EducationViewModel,
    intentUri: android.net.Uri?,
    openPassId: String?,
    maximizeBrightnessOnPassOpen: Boolean,
    onOpenPassHandled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Routes.INITIAL,
            modifier = modifier,
        ) {
            composable(Routes.INITIAL) {
                InitialScreen(
                    viewModel = previewViewModel,
                    intentUri = intentUri,
                    actions = InitialScreenActions(
                        shouldShowOnboarding = {
                            educationViewModel.shouldShowOnboarding(isExternalImport = intentUri != null)
                        },
                        onAppEntryStarted = {
                            educationViewModel.startAppEntry(isExternalImport = intentUri != null)
                        },
                        onImportArchive = { uri ->
                            gridViewModel.importWalletArchive(uri)
                        },
                        onNavigateToOnboarding = {
                            navController.navigate(Routes.ONBOARDING) {
                                popUpTo(Routes.INITIAL) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToGrid = {
                            navController.navigate(Routes.GRID) {
                                popUpTo(Routes.INITIAL) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToPreview = {
                            navController.navigate(Routes.PREVIEW) {
                                popUpTo(Routes.INITIAL) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    ),
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    bullets = listOf(
                        stringResource(AppR.string.onboarding_store_wallet_passes),
                        stringResource(AppR.string.onboarding_create_custom_passes),
                        stringResource(AppR.string.onboarding_refresh_reorder),
                    ),
                    titleContent = {
                        AppLogo(color = MaterialTheme.colorScheme.onSurface)
                    },
                    onContinue = {
                        educationViewModel.completeOnboarding()
                        navController.navigate(Routes.GRID) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(Routes.GRID) {
                PassGridScreen(
                    viewModel = gridViewModel,
                    educationViewModel = educationViewModel,
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onPreviewPass = { uri -> previewViewModel.previewPass(uri) },
                    openPassId = openPassId,
                    maximizeBrightnessOnPassOpen = maximizeBrightnessOnPassOpen,
                    onOpenPassHandled = onOpenPassHandled,
                    onSettingsClick = {
                        navController.navigate(Routes.SETTINGS)
                    },
                )
            }

            composable(
                route = Routes.SETTINGS,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(NAV_SLIDE_ANIMATION_DURATION_MS),
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(NAV_SLIDE_ANIMATION_DURATION_MS),
                    )
                },
            ) {
                SettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.PREVIEW) {
                PassPreviewScreen(
                    viewModel = previewViewModel,
                    onAdd = {
                        previewViewModel.confirmAddPass()
                        navController.navigate(Routes.GRID) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onCancel = {
                        previewViewModel.cancelPreview()
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.CAMERA_SCAN) {
                CameraScanScreen(
                    onScanResult = { scanResult ->
                        when (scanResult) {
                            is ScanResult.UrlDetected -> {
                                previewViewModel.downloadAndPreviewPass(scanResult.url)
                                navController.navigate(Routes.PREVIEW) {
                                    popUpTo(Routes.GRID) { inclusive = false }
                                }
                            }

                            is ScanResult.BarcodeDetected -> {
                                val formatName = getBarcodeFormatName(scanResult.format)
                                navController.navigate(
                                    Routes.customPassBuilder(
                                        scanResult.value,
                                        formatName,
                                    ),
                                ) {
                                    popUpTo(Routes.GRID) { inclusive = false }
                                }
                            }
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    },
                )
            }

            composable(
                route = Routes.CUSTOM_PASS_BUILDER,
                arguments = listOf(
                    navArgument("barcodeValue") { type = NavType.StringType },
                    navArgument("barcodeFormat") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val barcodeValue = backStackEntry.arguments?.getString("barcodeValue") ?: ""
                val barcodeFormat = backStackEntry.arguments?.getString("barcodeFormat") ?: ""

                CustomPassBuilderScreen(
                    barcodeValue = barcodeValue,
                    barcodeFormat = barcodeFormat,
                    onCancel = {
                        navController.popBackStack()
                    },
                    onPassCreated = {
                        navController.navigate(Routes.GRID) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}

/**
 * Convert ML Kit barcode format code to readable string name.
 */
private fun getBarcodeFormatName(format: Int): String = when (format) {
    Barcode.FORMAT_CODE_128 -> "CODE_128"
    Barcode.FORMAT_CODE_39 -> "CODE_39"
    Barcode.FORMAT_CODE_93 -> "CODE_93"
    Barcode.FORMAT_CODABAR -> "CODABAR"
    Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
    Barcode.FORMAT_EAN_13 -> "EAN_13"
    Barcode.FORMAT_EAN_8 -> "EAN_8"
    Barcode.FORMAT_ITF -> "ITF"
    Barcode.FORMAT_QR_CODE -> "QR_CODE"
    Barcode.FORMAT_UPC_A -> "UPC_A"
    Barcode.FORMAT_UPC_E -> "UPC_E"
    Barcode.FORMAT_PDF417 -> "PDF417"
    Barcode.FORMAT_AZTEC -> "AZTEC"
    else -> "UNKNOWN"
}

private const val NAV_SLIDE_ANIMATION_DURATION_MS = 300
