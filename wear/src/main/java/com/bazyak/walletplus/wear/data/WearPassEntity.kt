package com.bazyak.walletplus.wear.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wear_passes")
data class WearPassEntity(
    @PrimaryKey val id: String,
    val displayOrder: Int,
    val title: String,
    val subtitle: String,
    val snapshotJson: String,
    val iconPath: String?,
    val logoPath: String?,
    val updatedAt: Long,
)
