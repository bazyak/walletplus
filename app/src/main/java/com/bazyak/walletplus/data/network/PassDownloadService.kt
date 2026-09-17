package com.bazyak.walletplus.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bazyak.walletplus.data.model.WalletError
import com.bazyak.walletplus.data.model.walletErrorOr
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Service for downloading .pkpass files from URLs.
 */
class PassDownloadService(
    private val context: Context,
    private val okHttpClient: OkHttpClient = NetworkModule.okHttpClient,
) {

    /**
     * Result of a pass download operation.
     */
    sealed class DownloadResult {
        /** Pass downloaded successfully */
        data class Success(val fileUri: Uri) : DownloadResult()

        /** Network error occurred */
        data class Error(val error: WalletError) : DownloadResult()
    }

    /**
     * Downloads a .pkpass file from a URL to temporary storage.
     *
     * @param url The URL to download from
     * @return DownloadResult with the temporary file URI on success
     */
    suspend fun downloadPass(url: String): DownloadResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext DownloadResult.Error(
                    WalletError.DownloadFailed("HTTP ${response.code}"),
                )
            }

            val responseBody = response.body
            if (responseBody == null) {
                return@withContext DownloadResult.Error(WalletError.EmptyResponseBody)
            }

            // Save to temporary file
            val tempFile = File(
                context.cacheDir,
                "temp_download_${System.currentTimeMillis()}.pkpass",
            )

            FileOutputStream(tempFile).use { outputStream ->
                outputStream.write(responseBody.bytes())
            }

            Log.d("PassDownload", "Downloaded pass to ${tempFile.absolutePath}")
            DownloadResult.Success(Uri.fromFile(tempFile))
        } catch (e: SocketTimeoutException) {
            Log.e("PassDownload", "Download timed out", e)
            DownloadResult.Error(WalletError.RequestTimedOut)
        } catch (e: IOException) {
            Log.e("PassDownload", "Network error", e)
            DownloadResult.Error(WalletError.NoInternetConnection)
        } catch (e: Exception) {
            Log.e("PassDownload", "Download failed", e)
            DownloadResult.Error(e.walletErrorOr(WalletError.DownloadFailed("Unknown error")))
        }
    }
}
