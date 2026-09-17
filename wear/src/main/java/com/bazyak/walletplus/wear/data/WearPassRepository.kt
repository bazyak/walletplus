package com.bazyak.walletplus.wear.data

import android.content.Context
import com.bazyak.walletplus.wearsync.WearPassAssetType
import com.bazyak.walletplus.wearsync.WearPassIndex
import com.bazyak.walletplus.wearsync.WearPassSnapshot
import com.bazyak.walletplus.wearsync.WearSyncJson
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WearPassRepository(private val context: Context, private val dao: WearPassDao) {
    fun observePasses(): Flow<List<CachedWearPass>> = dao.observePasses().map { entities ->
        entities.mapNotNull { it.toCachedPassOrNull() }
    }

    fun observePass(passId: String): Flow<CachedWearPass?> =
        dao.observePass(passId).map { entity -> entity?.toCachedPassOrNull() }

    suspend fun upsertSnapshot(snapshot: WearPassSnapshot, iconBytes: ByteArray?, logoBytes: ByteArray?): Unit =
        withContext(Dispatchers.IO) {
            val iconPath = writeAsset(snapshot, WearPassAssetType.ICON, iconBytes)
            val logoPath = writeAsset(snapshot, WearPassAssetType.LOGO, logoBytes)

            dao.upsert(
                WearPassEntity(
                    id = snapshot.id,
                    displayOrder = snapshot.displayOrder,
                    title = snapshot.title,
                    subtitle = snapshot.subtitle,
                    snapshotJson = WearSyncJson.encodeSnapshot(snapshot),
                    iconPath = iconPath,
                    logoPath = logoPath,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }

    suspend fun applyIndex(index: WearPassIndex) = withContext(Dispatchers.IO) {
        val existing = dao.getAllPasses()
        val keepIds = index.passIds.toSet()

        existing
            .filterNot { it.id in keepIds }
            .forEach { entity -> passAssetDir(entity.id).deleteRecursively() }

        if (index.passIds.isEmpty()) {
            dao.deleteAll()
        } else {
            dao.deleteMissing(index.passIds)
        }
    }

    private fun WearPassEntity.toCachedPassOrNull(): CachedWearPass? = runCatching {
        CachedWearPass(
            snapshot = WearSyncJson.decodeSnapshot(snapshotJson),
            iconPath = iconPath,
            logoPath = logoPath,
        )
    }.getOrNull()

    private fun writeAsset(snapshot: WearPassSnapshot, type: WearPassAssetType, bytes: ByteArray?): String? {
        val asset = snapshot.assets.firstOrNull { it.type == type } ?: return null
        if (bytes == null) return null

        val file = File(passAssetDir(snapshot.id), asset.name)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        return file.absolutePath
    }

    private fun passAssetDir(passId: String): File = File(context.filesDir, "wear_pass_assets/$passId")
}
