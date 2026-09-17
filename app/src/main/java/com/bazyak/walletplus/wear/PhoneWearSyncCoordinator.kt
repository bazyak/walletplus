package com.bazyak.walletplus.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.bazyak.walletplus.data.model.Pass
import com.bazyak.walletplus.data.repository.PassRepository
import com.bazyak.walletplus.wearsync.WearPassIndex
import com.bazyak.walletplus.wearsync.WearSyncDataKeys
import com.bazyak.walletplus.wearsync.WearSyncJson
import com.bazyak.walletplus.wearsync.WearSyncPaths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PhoneWearSyncCoordinator(
    private val context: Context,
    private val dataClient: DataClient,
    private val nodeClient: NodeClient,
    private val passRepository: PassRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false
    private var wearApiUnavailable = false

    fun start() {
        if (started) return
        started = true

        scope.launch {
            passRepository.getAllPasses().collectLatest { passes ->
                publishPassesSafely(passes)
            }
        }
    }

    suspend fun publishNow() {
        if (wearApiUnavailable) return

        publishPassesSafely(passRepository.getAllPasses().first())
    }

    private suspend fun publishPassesSafely(passes: List<Pass>) {
        if (wearApiUnavailable) return

        runCatching {
            if (!canPublishToWear()) return

            publishPasses(passes)
        }.onFailure { exception ->
            if (exception.isWearableApiUnavailable()) {
                wearApiUnavailable = true
                Log.i(TAG, "Wearable API is unavailable; disabling phone-to-watch sync.", exception)
            } else {
                Log.w(TAG, "Failed to publish passes to Wear OS.", exception)
            }
        }
    }

    private suspend fun canPublishToWear(): Boolean {
        GoogleApiAvailability.getInstance()
            .checkApiAvailability(dataClient, nodeClient)
            .await()

        return nodeClient.getConnectedNodes().await().isNotEmpty()
    }

    private suspend fun publishPasses(passes: List<Pass>) = withContext(Dispatchers.IO) {
        val snapshotsByPass = passes.mapNotNull { pass ->
            pass.toWearPassSnapshot(context)?.let { snapshot -> pass to snapshot }
        }

        val index = WearPassIndex(
            passIds = snapshotsByPass.map { (_, snapshot) -> snapshot.id },
            updatedAt = System.currentTimeMillis(),
        )

        PutDataMapRequest.create(WearSyncPaths.PASSES_INDEX).apply {
            dataMap.putString(WearSyncDataKeys.INDEX_JSON, WearSyncJson.encodeIndex(index))
            dataMap.putLong(WearSyncDataKeys.UPDATED_AT, index.updatedAt)
        }.asPutDataRequest().setUrgent().also { request ->
            dataClient.putDataItem(request).await()
        }

        snapshotsByPass.forEach { (pass, snapshot) ->
            PutDataMapRequest.create(WearSyncPaths.pass(snapshot.id)).apply {
                dataMap.putString(WearSyncDataKeys.SNAPSHOT_JSON, WearSyncJson.encodeSnapshot(snapshot))
                dataMap.putLong(WearSyncDataKeys.UPDATED_AT, System.currentTimeMillis())
                snapshot.assets.forEach { assetRef ->
                    pass.assetFile(assetRef.type)?.let { file ->
                        dataMap.putAsset(assetRef.type.dataMapKey, Asset.createFromBytes(file.readBytes()))
                    }
                }
            }.asPutDataRequest().setUrgent().also { request ->
                dataClient.putDataItem(request).await()
            }
        }
    }
}

val com.bazyak.walletplus.wearsync.WearPassAssetType.dataMapKey: String
    get() = when (this) {
        com.bazyak.walletplus.wearsync.WearPassAssetType.ICON -> WearSyncDataKeys.ICON_ASSET
        com.bazyak.walletplus.wearsync.WearPassAssetType.LOGO -> WearSyncDataKeys.LOGO_ASSET
    }

private const val TAG = "PhoneWearSync"
