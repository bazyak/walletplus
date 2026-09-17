package com.bazyak.walletplus.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazyak.walletplus.data.model.ShareStatus
import com.bazyak.walletplus.data.model.WalletError
import com.bazyak.walletplus.data.model.walletErrorOr
import com.bazyak.walletplus.data.repository.WalletArchiveRepository
import com.bazyak.walletplus.settings.AppLanguageMode
import com.bazyak.walletplus.settings.AppThemeMode
import com.bazyak.walletplus.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val walletArchiveRepository: WalletArchiveRepository,
    private val context: Context,
) : ViewModel() {

    companion object {
        const val WALLET_ARCHIVE_SHARE_ID = "__wallet_archive__"
    }

    val themeMode: StateFlow<AppThemeMode> = settingsRepository.themeMode
    val languageMode: StateFlow<AppLanguageMode> = settingsRepository.languageMode
    val showEducations: StateFlow<Boolean> = settingsRepository.showEducations
    val maximizeBrightnessOnPassOpen: StateFlow<Boolean> = settingsRepository.maximizeBrightnessOnPassOpen

    private val _shareStatus = MutableStateFlow<ShareStatus>(ShareStatus.Idle)
    val shareStatus: StateFlow<ShareStatus> = _shareStatus.asStateFlow()

    fun setThemeMode(themeMode: AppThemeMode) {
        settingsRepository.setThemeMode(themeMode)
    }

    fun setLanguageMode(languageMode: AppLanguageMode) {
        settingsRepository.setLanguageMode(languageMode)
    }

    fun setShowEducations(enabled: Boolean) {
        settingsRepository.setShowEducations(enabled)
    }

    fun setMaximizeBrightnessOnPassOpen(enabled: Boolean) {
        settingsRepository.setMaximizeBrightnessOnPassOpen(enabled)
    }

    fun prepareShareWalletArchive() {
        viewModelScope.launch {
            _shareStatus.value = ShareStatus.Loading(WALLET_ARCHIVE_SHARE_ID)
            val result = walletArchiveRepository.exportAllPassesForSharing()

            _shareStatus.value = if (result.isSuccess) {
                ShareStatus.Success(result.getOrThrow(), WALLET_ARCHIVE_SHARE_ID)
            } else {
                ShareStatus.Error(
                    result.exceptionOrNull()
                        .walletErrorOr(WalletError.FailedToExportWallet)
                        .toMessage(context),
                    WALLET_ARCHIVE_SHARE_ID,
                )
            }
        }
    }

    fun resetShareStatus() {
        _shareStatus.value = ShareStatus.Idle
    }
}
