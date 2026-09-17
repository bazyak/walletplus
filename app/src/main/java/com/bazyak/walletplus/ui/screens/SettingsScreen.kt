package com.bazyak.walletplus.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.bazyak.walletplus.BuildConfig
import com.bazyak.walletplus.corestrings.R as AppR
import com.bazyak.walletplus.data.archive.WalletArchive
import com.bazyak.walletplus.data.exporter.ExportResult
import com.bazyak.walletplus.data.model.ShareStatus
import com.bazyak.walletplus.designsystem.R as DesignR
import com.bazyak.walletplus.designsystem.components.feedback.SnackbarStatus
import com.bazyak.walletplus.designsystem.components.feedback.WalletCircularProgressIndicator
import com.bazyak.walletplus.designsystem.components.feedback.WalletSnackbar
import com.bazyak.walletplus.designsystem.components.navigation.WalletTopAppBar
import com.bazyak.walletplus.designsystem.foundation.spacing.spacing
import com.bazyak.walletplus.settings.AppLanguageMode
import com.bazyak.walletplus.settings.AppThemeMode
import com.bazyak.walletplus.ui.components.settings.SettingsActionRow
import com.bazyak.walletplus.ui.components.settings.SettingsDropdownRow
import com.bazyak.walletplus.ui.components.settings.SettingsSectionTitle
import com.bazyak.walletplus.ui.components.settings.SettingsSelectorRow
import com.bazyak.walletplus.ui.components.settings.SettingsToggleRow
import com.bazyak.walletplus.ui.utils.sharePassFile
import com.bazyak.walletplus.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: SettingsViewModel = koinViewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    val languageMode by viewModel.languageMode.collectAsState()
    val showEducations by viewModel.showEducations.collectAsState()
    val maximizeBrightnessOnPassOpen by viewModel.maximizeBrightnessOnPassOpen.collectAsState()
    val shareStatus by viewModel.shareStatus.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val appInfo = "${stringResource(id = AppR.string.app_name)} ${BuildConfig.VERSION_NAME}"
    var pendingExportAction by remember { mutableStateOf<WalletBackupExportAction?>(null) }
    var pendingSaveResult by remember { mutableStateOf<ExportResult?>(null) }
    var saveSnackbarMessage by remember { mutableStateOf<String?>(null) }
    var saveSnackbarStatus by remember { mutableStateOf(SnackbarStatus.SUCCESS) }

    val saveWalletBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(WalletArchive.MIME_TYPE),
    ) { uri ->
        val exportResult = pendingSaveResult
        if (uri == null || exportResult == null) {
            pendingSaveResult = null
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            val saved = saveExportResult(context, exportResult, uri)
            saveSnackbarStatus = if (saved) SnackbarStatus.SUCCESS else SnackbarStatus.ERROR
            saveSnackbarMessage = if (saved) {
                context.getString(AppR.string.wallet_backup_saved)
            } else {
                context.getString(AppR.string.failed_to_save_wallet_backup)
            }
            pendingSaveResult = null
            delay(3000)
            saveSnackbarMessage = null
        }
    }

    LaunchedEffect(shareStatus) {
        when (val status = shareStatus) {
            is ShareStatus.Success -> {
                if (status.passId == SettingsViewModel.WALLET_ARCHIVE_SHARE_ID) {
                    when (pendingExportAction) {
                        WalletBackupExportAction.SHARE -> sharePassFile(context, status.exportResult)
                        WalletBackupExportAction.SAVE -> {
                            pendingSaveResult = status.exportResult
                            saveWalletBackupLauncher.launch(status.exportResult.file.name)
                        }
                        null -> Unit
                    }
                    pendingExportAction = null
                    viewModel.resetShareStatus()
                }
            }
            is ShareStatus.Error -> {
                // Snackbar is rendered from state below.
            }
            else -> { /* Idle or Loading */ }
        }
    }

    Scaffold(
        topBar = {
            WalletTopAppBar(
                title = { Text(stringResource(AppR.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = DesignR.drawable.arrow_back),
                            contentDescription = stringResource(AppR.string.back),
                        )
                    }
                },
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                SettingsSectionTitle(title = stringResource(AppR.string.appearance))
                SettingsSelectorRow(
                    title = stringResource(AppR.string.theme),
                    description = stringResource(AppR.string.theme_description),
                    options = AppThemeMode.entries,
                    selectedOption = themeMode,
                    optionLabel = { mode -> context.getString(mode.labelResId) },
                    onOptionSelected = viewModel::setThemeMode,
                    icon = DesignR.drawable.theme,
                )
                SettingsDropdownRow(
                    title = stringResource(AppR.string.language),
                    description = stringResource(AppR.string.language_description),
                    options = AppLanguageMode.entries,
                    selectedOption = languageMode,
                    optionLabel = { mode -> context.getString(mode.labelResId) },
                    onOptionSelected = viewModel::setLanguageMode,
                    icon = DesignR.drawable.language,
                )
                SettingsToggleRow(
                    title = stringResource(AppR.string.maximize_brightness_on_pass_open),
                    description = stringResource(AppR.string.maximize_brightness_on_pass_open_description),
                    checked = maximizeBrightnessOnPassOpen,
                    onCheckedChange = viewModel::setMaximizeBrightnessOnPassOpen,
                    icon = DesignR.drawable.brightness,
                )

                SettingsSectionTitle(title = stringResource(AppR.string.backup))
                SettingsActionRow(
                    title = stringResource(AppR.string.share_wallet_backup),
                    description = stringResource(AppR.string.share_wallet_backup_description),
                    onClick = {
                        pendingExportAction = WalletBackupExportAction.SHARE
                        viewModel.prepareShareWalletArchive()
                    },
                    icon = DesignR.drawable.share,
                )
                SettingsActionRow(
                    title = stringResource(AppR.string.save_wallet_backup),
                    description = stringResource(AppR.string.save_wallet_backup_description),
                    onClick = {
                        pendingExportAction = WalletBackupExportAction.SAVE
                        viewModel.prepareShareWalletArchive()
                    },
                    icon = DesignR.drawable.file_export,
                )

                if (BuildConfig.DEBUG) {
                    SettingsSectionTitle(title = stringResource(AppR.string.dev))
                    SettingsToggleRow(
                        title = stringResource(AppR.string.show_educations),
                        description = stringResource(AppR.string.show_educations_description),
                        checked = showEducations,
                        onCheckedChange = viewModel::setShowEducations,
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.massive))
                Text(
                    text = appInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.spacing.mediumLarge,
                            end = MaterialTheme.spacing.mediumLarge,
                            bottom = MaterialTheme.spacing.extraLarge,
                        ),
                )
            }

            if (
                shareStatus is ShareStatus.Loading &&
                (shareStatus as ShareStatus.Loading).passId == SettingsViewModel.WALLET_ARCHIVE_SHARE_ID
            ) {
                WalletCircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (
                shareStatus is ShareStatus.Error &&
                (shareStatus as ShareStatus.Error).passId == SettingsViewModel.WALLET_ARCHIVE_SHARE_ID
            ) {
                WalletSnackbar(
                    message = (shareStatus as ShareStatus.Error).message,
                    status = SnackbarStatus.ERROR,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(MaterialTheme.spacing.mediumLarge),
                )
            }

            saveSnackbarMessage?.let { message ->
                WalletSnackbar(
                    message = message,
                    status = saveSnackbarStatus,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(MaterialTheme.spacing.mediumLarge),
                )
            }
        }
    }
}

private enum class WalletBackupExportAction {
    SHARE,
    SAVE,
}

private suspend fun saveExportResult(context: android.content.Context, exportResult: ExportResult, uri: Uri): Boolean =
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                exportResult.file.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return@withContext false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
