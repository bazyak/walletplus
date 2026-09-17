package com.bazyak.walletplus.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bazyak.walletplus.data.archive.WalletArchive
import com.bazyak.walletplus.designsystem.components.branding.AppLogo
import com.bazyak.walletplus.designsystem.components.feedback.WalletCircularProgressIndicator
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.ui.viewmodel.PassPreviewViewModel

/**
 * Initial loading screen that decides whether to navigate to grid or preview.
 * Clears backstack when navigating to prevent going back to this screen.
 */
@Composable
fun InitialScreen(
    viewModel: PassPreviewViewModel,
    intentUri: Uri?,
    actions: InitialScreenActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppLogo(color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
        WalletCircularProgressIndicator()
    }

    LaunchedEffect(Unit) {
        actions.onAppEntryStarted()
        if (intentUri != null) {
            if (WalletArchive.isWalletArchiveUri(context, intentUri)) {
                actions.onImportArchive(intentUri)
                actions.onNavigateToGrid()
            } else {
                // Launch with intent - start preview loading and navigate
                viewModel.previewPass(intentUri)
                actions.onNavigateToPreview()
            }
        } else if (actions.shouldShowOnboarding()) {
            actions.onNavigateToOnboarding()
        } else {
            // Normal launch - go straight to grid
            actions.onNavigateToGrid()
        }
    }
}

data class InitialScreenActions(
    val shouldShowOnboarding: () -> Boolean,
    val onAppEntryStarted: () -> Unit,
    val onImportArchive: (Uri) -> Unit,
    val onNavigateToOnboarding: () -> Unit,
    val onNavigateToGrid: () -> Unit,
    val onNavigateToPreview: () -> Unit,
)
