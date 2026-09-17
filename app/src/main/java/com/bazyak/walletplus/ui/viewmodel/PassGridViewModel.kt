package com.bazyak.walletplus.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.RefreshStatus
import com.bazyak.walletplus.data.model.ShareStatus
import com.bazyak.walletplus.data.model.WalletError
import com.bazyak.walletplus.data.model.walletErrorOr
import com.bazyak.walletplus.data.network.PKPassUpdateService
import com.bazyak.walletplus.data.repository.PassRepository
import com.bazyak.walletplus.data.repository.WalletArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PassGridViewModel(
    private val passRepository: PassRepository,
    private val walletArchiveRepository: WalletArchiveRepository,
    private val importStatusHolder: ImportStatusHolder,
    private val context: Context,
) : ViewModel() {

    val importStatus: StateFlow<ImportStatus> = importStatusHolder.importStatus

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    val passes: StateFlow<List<Pass>> = passRepository.getAllPasses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    init {
        viewModelScope.launch {
            passes.first()
            _isInitialLoading.value = false
        }
    }

    private val _refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.Idle)
    val refreshStatus: StateFlow<RefreshStatus> = _refreshStatus.asStateFlow()

    private val _shareStatus = MutableStateFlow<ShareStatus>(ShareStatus.Idle)
    val shareStatus: StateFlow<ShareStatus> = _shareStatus.asStateFlow()

    fun refreshPass(passId: String) {
        viewModelScope.launch {
            _refreshStatus.value = RefreshStatus.Loading(passId)
            val result = passRepository.refreshPass(passId)

            if (result.isSuccess) {
                val updateResult = result.getOrThrow()
                _refreshStatus.value = when (updateResult) {
                    is PKPassUpdateService.UpdateResult.Updated -> RefreshStatus.Success(1)
                    is PKPassUpdateService.UpdateResult.NotModified -> RefreshStatus.Success(0)
                    is PKPassUpdateService.UpdateResult.Deleted ->
                        RefreshStatus.Error(WalletError.PassWasDeleted.toMessage(context), passId)
                    is PKPassUpdateService.UpdateResult.Unauthorized ->
                        RefreshStatus.Error(WalletError.UpdateAuthorizationFailed.toMessage(context), passId)
                    is PKPassUpdateService.UpdateResult.NetworkError ->
                        RefreshStatus.Error(updateResult.error.toMessage(context), passId)
                    is PKPassUpdateService.UpdateResult.NoWebService ->
                        RefreshStatus.Error(WalletError.PassUpdatesUnsupported.toMessage(context), passId)
                }
            } else {
                _refreshStatus.value = RefreshStatus.Error(
                    result.exceptionOrNull()
                        .walletErrorOr(WalletError.Unknown)
                        .toMessage(context),
                    passId,
                )
            }

            kotlinx.coroutines.delay(2000)
            _refreshStatus.value = RefreshStatus.Idle
        }
    }

    fun refreshAllPasses() {
        viewModelScope.launch {
            _refreshStatus.value = RefreshStatus.Loading(null)
            val result = passRepository.refreshAllPasses()

            // A batch refresh reports how many passes actually changed. Per-pass failures are
            // skipped on purpose: one issuer being rate limited, unreachable, or serving a broken
            // payload should not replace the result for every other pass with its error.
            _refreshStatus.value = if (result.isSuccess) {
                val results = result.getOrThrow()
                RefreshStatus.Success(
                    results.values.count { it is PKPassUpdateService.UpdateResult.Updated },
                )
            } else {
                RefreshStatus.Error(
                    result.exceptionOrNull()
                        .walletErrorOr(WalletError.Unknown)
                        .toMessage(context),
                    null,
                )
            }

            kotlinx.coroutines.delay(2000)
            _refreshStatus.value = RefreshStatus.Idle
        }
    }

    fun deletePass(pass: Pass) {
        viewModelScope.launch {
            passRepository.deletePass(pass)
        }
    }

    fun updatePassOrder(passOrderMap: Map<String, Int>) {
        viewModelScope.launch {
            passRepository.updateDisplayOrders(passOrderMap)
        }
    }

    suspend fun getPassById(passId: String): Pass? = passRepository.getPassById(passId)

    fun setAutoRefreshEnabled(passId: String, enabled: Boolean) {
        viewModelScope.launch {
            passRepository.setAutoRefreshEnabled(passId, enabled)
        }
    }

    fun prepareSharePass(passId: String) {
        viewModelScope.launch {
            _shareStatus.value = ShareStatus.Loading(passId)
            val result = passRepository.exportPassForSharing(passId)

            _shareStatus.value = if (result.isSuccess) {
                ShareStatus.Success(result.getOrThrow(), passId)
            } else {
                ShareStatus.Error(
                    result.exceptionOrNull()
                        .walletErrorOr(WalletError.FailedToSharePass)
                        .toMessage(context),
                    passId,
                )
            }
        }
    }

    fun importWalletArchive(uri: Uri) {
        viewModelScope.launch {
            importStatusHolder.setImportStatus(ImportStatus.Loading)
            val result = walletArchiveRepository.importWalletArchive(uri)
            importStatusHolder.setImportStatus(
                if (result.isSuccess) {
                    ImportStatus.Summary(result.getOrThrow().localizedSummary(context))
                } else {
                    ImportStatus.Error(
                        result.exceptionOrNull()
                            .walletErrorOr(WalletError.FailedToImportWalletArchive)
                            .toMessage(context),
                    )
                },
            )

            kotlinx.coroutines.delay(3000)
            importStatusHolder.setImportStatus(ImportStatus.Idle)
        }
    }

    fun resetShareStatus() {
        _shareStatus.value = ShareStatus.Idle
    }
}
