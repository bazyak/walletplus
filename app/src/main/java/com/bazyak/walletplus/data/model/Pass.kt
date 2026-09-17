package com.bazyak.walletplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Format-agnostic pass entity stored in Room database.
 * Supports multiple pass formats through abstraction.
 */
@Entity(tableName = "passes")
data class Pass(
    @PrimaryKey
    val id: String,

    /** Format of this pass (PKPASS, GOOGLE_WALLET, etc.) */
    val format: PassFormat,

    /** Organization name (e.g., airline, store, venue) */
    val organizationName: String,

    /** Pass description/title */
    val description: String,

    /** Path to icon image in internal storage */
    val iconPath: String,

    /** Path to logo image in internal storage */
    val logoPath: String? = null,

    /** Path to strip image in internal storage */
    val stripPath: String? = null,

    /** Path to background image in internal storage */
    val backgroundPath: String? = null,

    /** Foreground color in hex format (e.g., "#FFFFFF") */
    val foregroundColor: String?,

    /** Background color in hex format (e.g., "#000000") */
    val backgroundColor: String?,

    /** Label color in hex format (e.g., "#999999") */
    val labelColor: String?,

    /** Directory containing format-specific assets */
    val assetsDirectory: String,

    /** JSON blob of format-specific data */
    val rawData: String,

    /** Timestamp when pass was imported */
    val importedDate: Long,

    /** Timestamp of last successful refresh (null if never refreshed) */
    val lastRefreshDate: Long? = null,

    /** Whether automatic background refresh is enabled for this pass */
    val autoRefreshEnabled: Boolean = true,

    /** Display order for grid arrangement (lower values appear first) */
    val displayOrder: Int = 0,

    /** Category of the pass */
    val category: PassCategory,
)

/**
 * Pass categories (based on PKPass types, extensible for other formats).
 */
enum class PassCategory {
    BOARDING_PASS,
    EVENT_TICKET,
    COUPON,
    STORE_CARD,
    GENERIC,
}
