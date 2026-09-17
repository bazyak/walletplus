package com.bazyak.walletplus.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bazyak.walletplus.data.local.PassDatabase
import com.bazyak.walletplus.data.model.PassFormat
import com.bazyak.walletplus.data.model.WalletError
import com.bazyak.walletplus.data.network.PKPassUpdateService
import com.bazyak.walletplus.data.parser.ParserRegistry
import com.bazyak.walletplus.data.repository.PassRepositoryImpl

/**
 * Background worker for refreshing passes daily.
 * Runs once per day when WiFi is available and battery is not low.
 */
class PassRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        // Instantiate repository manually (no DI framework)
        val database = PassDatabase.getInstance(applicationContext)
        val parserRegistry = ParserRegistry(applicationContext)
        val repository = PassRepositoryImpl(
            passDao = database.passDao(),
            parserRegistry = parserRegistry,
            context = applicationContext,
        )

        // Get all passes and filter only those with autoRefreshEnabled = true
        val allPasses = database.passDao().getAllPassesList()
        val passesToRefresh = allPasses.filter { pass ->
            pass.autoRefreshEnabled &&
                pass.format == PassFormat.PKPASS
        }

        // Refresh each enabled pass. A failure on one pass must never stop the others, and only a
        // transient failure justifies rerunning the whole batch: retrying a pass whose issuer
        // serves a malformed payload or rejects our token would repeat forever, and periodic work
        // reruns tomorrow anyway.
        var hasTransientFailure = false
        passesToRefresh.forEach { pass ->
            val result = repository.refreshPass(pass.id)
            val outcome = result.getOrNull()
            val transient = when {
                result.isFailure -> true
                outcome is PKPassUpdateService.UpdateResult.NetworkError -> outcome.error.isTransient()
                else -> false
            }

            if (transient) {
                hasTransientFailure = true
            }
        }

        if (hasTransientFailure && runAttemptCount < MAX_RUN_ATTEMPTS) {
            // Retry with exponential backoff while the failure still looks recoverable
            Result.retry()
        } else {
            Result.success()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        if (runAttemptCount < MAX_RUN_ATTEMPTS) Result.retry() else Result.success()
    }

    private fun WalletError.isTransient(): Boolean = when (this) {
        WalletError.NoInternetConnection, WalletError.RequestTimedOut -> true
        is WalletError.UpdateFailed -> cause.isTransient()
        else -> false
    }

    private companion object {
        const val MAX_RUN_ATTEMPTS = 3
    }
}
