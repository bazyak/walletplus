package com.bazyak.walletplus.ui.utils

import androidx.annotation.DrawableRes
import com.bazyak.walletplus.designsystem.R

/**
 * Utility object for mapping icon names to custom drawable icons.
 * Used for custom pass icon selection and rendering.
 */
object IconMapper {
    /**
     * List of available icons for custom pass selection.
     * Each pair contains: (iconName, drawableResourceId)
     */
    val availableIcons: List<Pair<String, Int>> = listOf(
        "Star" to R.drawable.star,
        "Like" to R.drawable.like,
        "Cart" to R.drawable.cart,
        "Gift" to R.drawable.present,
        "Food" to R.drawable.food,
        "Shop" to R.drawable.shop,
        "Flower" to R.drawable.flower,
        "Package" to R.drawable.package_icon,
    )

    /**
     * Retrieves the drawable resource ID for a given icon name.
     *
     * @param name The icon name (e.g., "Star", "Cart")
     * @return The corresponding drawable resource ID, or Star as fallback if not found
     */
    @DrawableRes
    fun getIconByName(name: String?): Int {
        return availableIcons.find { it.first == name }?.second
            ?: R.drawable.star // Default fallback
    }
}
