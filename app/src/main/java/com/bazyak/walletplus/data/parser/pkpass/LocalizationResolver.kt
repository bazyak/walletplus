package com.bazyak.walletplus.data.parser.pkpass

import java.util.Locale

/**
 * Resolves localized values for PKPass strings with fallback chain:
 * 1. Device locale (language code)
 * 2. English ("en")
 * 3. Raw value
 */
object LocalizationResolver {

    /**
     * Resolves a localized value using the fallback chain
     *
     * @param value The raw value (may contain localization keys or direct text)
     * @param localizations The PKPass localizations map
     * @param locale The device locale to use for resolution
     * @return The localized value or the original value if no localization is found
     */
    fun resolveLocalizedValue(
        value: String?,
        localizations: PKPassLocalizations?,
        locale: Locale = Locale.getDefault(),
    ): String {
        // Return empty string for null/empty values
        if (value.isNullOrEmpty()) {
            return ""
        }

        // If no localizations available, return raw value
        if (localizations == null || localizations.localizations.isEmpty()) {
            return value
        }

        // Try to find localized value using fallback chain
        val languageCode = locale.language

        // 1. Try device locale
        val deviceLocaleMap = localizations.localizations[languageCode]
        if (deviceLocaleMap != null) {
            val localizedValue = deviceLocaleMap[value]
            if (localizedValue != null) {
                return localizedValue
            }
        }

        // 2. Try English fallback
        if (languageCode != "en") {
            val englishMap = localizations.localizations["en"]
            if (englishMap != null) {
                val localizedValue = englishMap[value]
                if (localizedValue != null) {
                    return localizedValue
                }
            }
        }

        // 3. Try default locale (if specified and different from above)
        if (localizations.defaultLocale != languageCode && localizations.defaultLocale != "en") {
            val defaultMap = localizations.localizations[localizations.defaultLocale]
            if (defaultMap != null) {
                val localizedValue = defaultMap[value]
                if (localizedValue != null) {
                    return localizedValue
                }
            }
        }

        // 4. Fall back to raw value
        return value
    }

    /**
     * Resolves localized values for a list of fields
     * Applies localization to both labels and values
     */
    fun resolveFieldValues(
        fields: List<PKField>?,
        localizations: PKPassLocalizations?,
        locale: Locale = Locale.getDefault(),
    ): List<PKField>? {
        if (fields == null) return null

        return fields.map { field ->
            field.copy(
                label = resolveLocalizedValue(field.label, localizations, locale),
                value = resolveLocalizedValue(field.value?.toString(), localizations, locale),
            )
        }
    }
}
