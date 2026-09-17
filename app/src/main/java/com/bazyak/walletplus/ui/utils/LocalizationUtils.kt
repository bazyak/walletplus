package com.bazyak.walletplus.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.bazyak.walletplus.data.parser.pkpass.LocalizationResolver
import com.bazyak.walletplus.data.parser.pkpass.PKPassJson
import java.util.Locale

/**
 * Composable utilities for PKPass localization
 */

/**
 * Remembers a localized value using the app's current locale
 * with fallback chain: app locale → English → raw value
 *
 * @param value The raw value (may be a localization key or direct text)
 * @param pkPassJson The PKPass JSON containing localization data
 * @return Localized string or the original value if no localization is found
 */
@Composable
fun rememberLocalizedValue(value: String?, pkPassJson: PKPassJson?): String {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0] ?: Locale.getDefault()

    return remember(value, pkPassJson, locale) {
        LocalizationResolver.resolveLocalizedValue(
            value = value,
            localizations = pkPassJson?.localizations,
            locale = locale,
        )
    }
}

/**
 * Extension function to get localized field value for PKPassField
 */
@Composable
fun rememberLocalizedField(label: String?, value: Any?, pkPassJson: PKPassJson?): Pair<String, String> {
    val localizedLabel = rememberLocalizedValue(label, pkPassJson)
    val localizedValue = rememberLocalizedValue(value?.toString(), pkPassJson)
    return Pair(localizedLabel, localizedValue)
}
