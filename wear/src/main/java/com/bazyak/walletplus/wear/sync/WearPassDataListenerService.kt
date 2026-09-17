package com.bazyak.walletplus.wear.sync

import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.bazyak.walletplus.wear.data.WearPassRepository
import com.bazyak.walletplus.wearsync.WearSyncDataKeys
import com.bazyak.walletplus.wearsync.WearSyncJson
import com.bazyak.walletplus.wearsync.WearSyncPaths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class WearPassDataListenerService : WearableListenerService() {
    private val dataClient: DataClient by inject()
    private val repository: WearPassRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach

            val path = event.dataItem.uri.path ?: return@forEach
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

            when {
                path == WearSyncPaths.PASSES_INDEX -> {
                    val indexJson = dataMap.getString(WearSyncDataKeys.INDEX_JSON) ?: return@forEach
                    scope.launch {
                        runCatching {
                            repository.applyIndex(WearSyncJson.decodeIndex(indexJson))
                        }
                    }
                }
                WearSyncPaths.passIdFromPath(path) != null -> {
                    val snapshotJson = dataMap.getString(WearSyncDataKeys.SNAPSHOT_JSON) ?: return@forEach
                    val iconAsset = dataMap.getAsset(WearSyncDataKeys.ICON_ASSET)
                    val logoAsset = dataMap.getAsset(WearSyncDataKeys.LOGO_ASSET)
                    scope.launch {
                        runCatching {
                            repository.upsertSnapshot(
                                snapshot = WearSyncJson.decodeSnapshot(snapshotJson),
                                iconBytes = readAssetBytes(iconAsset),
                                logoBytes = readAssetBytes(logoAsset),
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun readAssetBytes(asset: Asset?): ByteArray? = withContext(Dispatchers.IO) {
        asset ?: return@withContext null
        val response = dataClient.getFdForAsset(asset).await()
        response.inputStream.use { inputStream ->
            inputStream.readBytes()
        }
    }
}
