package com.bazyak.walletplus.wear.sync

import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.bazyak.walletplus.wearsync.WearSyncPaths
import kotlinx.coroutines.tasks.await

class WearPhonePassOpener(private val messageClient: MessageClient, private val nodeClient: NodeClient) {
    suspend fun openPassOnPhone(passId: String): Boolean {
        val nodes = runCatching { nodeClient.connectedNodes.await() }.getOrDefault(emptyList())
        if (nodes.isEmpty()) return false

        val payload = passId.encodeToByteArray()
        return nodes.any { node ->
            runCatching {
                messageClient.sendMessage(
                    node.id,
                    WearSyncPaths.OPEN_PASS_ON_PHONE,
                    payload,
                ).await()
            }.isSuccess
        }
    }
}
