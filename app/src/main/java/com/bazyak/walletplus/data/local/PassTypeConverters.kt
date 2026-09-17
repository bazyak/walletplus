package com.bazyak.walletplus.data.local

import androidx.room.TypeConverter
import com.bazyak.walletplus.data.model.PassCategory
import com.bazyak.walletplus.data.model.PassFormat

/**
 * Type converters for Room database to handle enum types.
 */
class PassTypeConverters {
    @TypeConverter
    fun fromPassFormat(value: PassFormat): String = value.name

    @TypeConverter
    fun toPassFormat(value: String): PassFormat = PassFormat.valueOf(value)

    @TypeConverter
    fun fromPassCategory(value: PassCategory): String = value.name

    @TypeConverter
    fun toPassCategory(value: String): PassCategory = PassCategory.valueOf(value)
}
