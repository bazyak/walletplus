package com.bazyak.walletplus.wear

import android.content.Intent
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.bazyak.walletplus.MainActivity
import com.bazyak.walletplus.wearsync.DotWalletPassDeepLink
import com.bazyak.walletplus.wearsync.WearSyncPaths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PhoneWearSyncListenerService : WearableListenerService() {
    private val wearSyncCoordinator: PhoneWearSyncCoordinator by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        if (dataEvents.any { event ->
                event.type == DataEvent.TYPE_CHANGED &&
                    event.dataItem.uri.path == WearSyncPaths.REQUEST_SYNC
            }
        ) {
            scope.launch {
                wearSyncCoordinator.publishNow()
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != WearSyncPaths.OPEN_PASS_ON_PHONE) return

        val passId = messageEvent.data
            .decodeToString()
            .takeIf { it.isNotBlank() }
            ?: return

        startActivity(
            Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = DotWalletPassDeepLink.buildOpenPassUri(passId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
        )
    }
}
