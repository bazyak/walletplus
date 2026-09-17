package com.bazyak.walletplus

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.bazyak.walletplus.data.archive.WalletArchive
import com.bazyak.walletplus.data.worker.PassRefreshWorker
import com.bazyak.walletplus.designsystem.theme.WalletTheme
import com.bazyak.walletplus.settings.AppThemeMode
import com.bazyak.walletplus.ui.navigation.PassNavGraph
import com.bazyak.walletplus.ui.navigation.Routes
import com.bazyak.walletplus.ui.viewmodel.EducationViewModel
import com.bazyak.walletplus.ui.viewmodel.PassGridViewModel
import com.bazyak.walletplus.ui.viewmodel.PassPreviewViewModel
import com.bazyak.walletplus.ui.viewmodel.SettingsViewModel
import com.bazyak.walletplus.wearsync.DotWalletPassDeepLink
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val gridViewModel: PassGridViewModel by viewModel()
    private val previewViewModel: PassPreviewViewModel by viewModel()
    private val educationViewModel: EducationViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by viewModel()

    private var intentUri by mutableStateOf<Uri?>(null)
    private var newIntentUri by mutableStateOf<Uri?>(null)
    private var openPassId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If launched from external app with ACTION_VIEW, relaunch in our own task
        if (intent?.action == Intent.ACTION_VIEW && !isTaskRoot) {
            val relaunchIntent = Intent(this, MainActivity::class.java).apply {
                action = intent.action
                data = intent.data
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(relaunchIntent)
            finish()
            return
        }

        enableEdgeToEdge()

        // Clean up temporary preview files on startup
        lifecycleScope.launch {
            File(cacheDir, "preview_passes").deleteRecursively()
            File(cacheDir, "shared_passes").deleteRecursively()
        }

        // Schedule background pass refresh
        schedulePassRefresh()

        val initialOpenPassId = intent?.openPassDeepLinkId()
        if (initialOpenPassId != null) {
            clearActivityIntent()
        }

        // Extract import URI if present. Dot Wallet pass links are handled as one-shot navigation events.
        intentUri = if (initialOpenPassId == null && intent?.action == Intent.ACTION_VIEW) {
            intent.data
        } else {
            null
        }
        openPassId = initialOpenPassId

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val maximizeBrightnessOnPassOpen by settingsViewModel.maximizeBrightnessOnPassOpen.collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> systemDarkTheme
            }

            SideEffect {
                val backgroundColor = if (darkTheme) Color.BLACK else Color.WHITE
                window.setBackgroundDrawable(ColorDrawable(backgroundColor))
                window.decorView.setBackgroundColor(backgroundColor)
            }

            WalletTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                PassNavGraph(
                    navController = navController,
                    gridViewModel = gridViewModel,
                    previewViewModel = previewViewModel,
                    educationViewModel = educationViewModel,
                    intentUri = intentUri,
                    openPassId = openPassId,
                    maximizeBrightnessOnPassOpen = maximizeBrightnessOnPassOpen,
                    onOpenPassHandled = {
                        openPassId = null
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                LaunchedEffect(openPassId) {
                    if (openPassId != null) {
                        educationViewModel.startAppEntry(isExternalImport = false)
                        navController.navigate(Routes.GRID) {
                            popUpTo(Routes.INITIAL) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                // Handle new intents while app is running
                LaunchedEffect(newIntentUri) {
                    newIntentUri?.let { uri ->
                        educationViewModel.startAppEntry(isExternalImport = true)
                        if (WalletArchive.isWalletArchiveUri(this@MainActivity, uri)) {
                            gridViewModel.importWalletArchive(uri)
                            navController.navigate(Routes.GRID) {
                                popUpTo(Routes.GRID) { inclusive = false }
                                launchSingleTop = true
                            }
                        } else {
                            previewViewModel.previewPass(uri)
                            navController.navigate(Routes.PREVIEW) {
                                popUpTo(Routes.GRID) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        newIntentUri = null
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent.openPassDeepLinkId()?.let { passId ->
            openPassId = passId
            clearActivityIntent()
            return
        }

        setIntent(intent)

        // Handle new import intent while app is running
        if (intent.action == Intent.ACTION_VIEW) {
            newIntentUri = intent.data
        }
    }

    /**
     * Schedules daily background refresh of passes.
     * Runs once per day when WiFi is available and battery is not low.
     */
    private fun schedulePassRefresh() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only
            .setRequiresBatteryNotLow(true)
            .build()

        val refreshRequest = PeriodicWorkRequestBuilder<PassRefreshWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
            flexTimeInterval = 2,
            flexTimeIntervalUnit = TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "pass_refresh",
                ExistingPeriodicWorkPolicy.KEEP,
                refreshRequest,
            )
    }

    private fun Intent.openPassDeepLinkId(): String? = takeIf { action == Intent.ACTION_VIEW }
        ?.data
        ?.let(DotWalletPassDeepLink::passIdFromUri)

    private fun clearActivityIntent() {
        setIntent(Intent(this, MainActivity::class.java))
    }
}
