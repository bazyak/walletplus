package com.bazyak.walletplus.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.WalletError
import com.bazyak.walletplus.data.model.walletErrorOr
import com.bazyak.walletplus.data.repository.PassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PassPreviewViewModel(
    private val passRepository: PassRepository,
    private val importStatusHolder: ImportStatusHolder,
    private val context: Context,
) : ViewModel() {

    private val _previewPass = MutableStateFlow<Pass?>(null)
    val previewPass: StateFlow<Pass?> = _previewPass.asStateFlow()

    private val _previewStatus = MutableStateFlow<PreviewStatus>(PreviewStatus.Idle)
    val previewStatus: StateFlow<PreviewStatus> = _previewStatus.asStateFlow()

    fun previewPass(uri: Uri) {
        viewModelScope.launch {
            _previewPass.value?.let { existingPass ->
                passRepository.cleanupPreviewAssets(existingPass)
            }

            _previewStatus.value = PreviewStatus.Loading
            val result = passRepository.parsePassForPreview(uri)

            if (result.isSuccess) {
                _previewPass.value = result.getOrThrow()
                _previewStatus.value = PreviewStatus.Ready
            } else {
                _previewPass.value = null
                _previewStatus.value = PreviewStatus.Error(
                    result.exceptionOrNull()
                        .walletErrorOr(WalletError.FailedToLoadPass)
                        .toMessage(context),
                )
            }
        }
    }

    fun confirmAddPass() {
        viewModelScope.launch {
            val pass = _previewPass.value ?: return@launch

            _previewStatus.value = PreviewStatus.Loading
            val result = passRepository.finalizePassImport(pass)

            if (result.isSuccess) {
                _previewPass.value = null
                _previewStatus.value = PreviewStatus.Idle
                importStatusHolder.setImportStatus(ImportStatus.Success)
                kotlinx.coroutines.delay(2000)
                importStatusHolder.setImportStatus(ImportStatus.Idle)
            } else {
                _previewStatus.value = PreviewStatus.Error(
                    result.exceptionOrNull()
                        .walletErrorOr(WalletError.FailedToAddPass)
                        .toMessage(context),
                )
            }
        }
    }

    fun cancelPreview() {
        viewModelScope.launch {
            _previewPass.value?.let { pass ->
                passRepository.cleanupPreviewAssets(pass)
            }
            _previewPass.value = null
            _previewStatus.value = PreviewStatus.Idle
        }
    }

    fun downloadAndPreviewPass(url: String) {
        viewModelScope.launch {
            _previewPass.value?.let { existingPass ->
                passRepository.cleanupPreviewAssets(existingPass)
            }

            _previewStatus.value = PreviewStatus.Loading
            val result = passRepository.downloadAndPreviewPass(url)

            if (result.isSuccess) {
                _previewPass.value = result.getOrThrow()
                _previewStatus.value = PreviewStatus.Ready
            } else {
                _previewPass.value = null
                _previewStatus.value = PreviewStatus.Error(
                    result.exceptionOrNull()
                        .walletErrorOr(WalletError.FailedToDownloadPass)
                        .toMessage(context),
                )
            }
        }
    }
}
