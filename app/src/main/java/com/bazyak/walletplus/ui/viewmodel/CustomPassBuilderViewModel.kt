package com.bazyak.walletplus.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.model.WalletError
import com.bazyak.walletplus.data.model.walletErrorOr
import com.bazyak.walletplus.data.repository.PassRepository
import kotlinx.coroutines.launch

class CustomPassBuilderViewModel(
    private val passRepository: PassRepository,
    private val importStatusHolder: ImportStatusHolder,
    private val context: Context,
) : ViewModel() {

    fun createCustomPass(pass: Pass) {
        viewModelScope.launch {
            importStatusHolder.setImportStatus(ImportStatus.Loading)
            val result = passRepository.createCustomPass(pass)
            importStatusHolder.setImportStatus(
                if (result.isSuccess) {
                    ImportStatus.Success
                } else {
                    ImportStatus.Error(
                        result.exceptionOrNull()
                            .walletErrorOr(WalletError.Unknown)
                            .toMessage(context),
                    )
                },
            )

            kotlinx.coroutines.delay(2000)
            importStatusHolder.setImportStatus(ImportStatus.Idle)
        }
    }
}
