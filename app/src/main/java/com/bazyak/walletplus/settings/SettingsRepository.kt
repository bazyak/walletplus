package com.bazyak.walletplus.settings

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.LocaleList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsRepository {
    val themeMode: StateFlow<AppThemeMode>
    val languageMode: StateFlow<AppLanguageMode>
    val showEducations: StateFlow<Boolean>
    val maximizeBrightnessOnPassOpen: StateFlow<Boolean>

    fun setThemeMode(themeMode: AppThemeMode)

    fun setLanguageMode(languageMode: AppLanguageMode)

    fun setShowEducations(enabled: Boolean)

    fun setMaximizeBrightnessOnPassOpen(enabled: Boolean)
}

class SharedPreferencesSettingsRepository(context: Context) : SettingsRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val localeManager = context.getSystemService(LocaleManager::class.java)

    private val _themeMode = MutableStateFlow(readThemeMode())
    override val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _languageMode = MutableStateFlow(readLanguageMode())
    override val languageMode: StateFlow<AppLanguageMode> = _languageMode.asStateFlow()

    private val _showEducations = MutableStateFlow(preferences.getBoolean(KEY_SHOW_EDUCATIONS, false))
    override val showEducations: StateFlow<Boolean> = _showEducations.asStateFlow()

    private val _maximizeBrightnessOnPassOpen =
        MutableStateFlow(preferences.getBoolean(KEY_MAXIMIZE_BRIGHTNESS_ON_PASS_OPEN, false))
    override val maximizeBrightnessOnPassOpen: StateFlow<Boolean> = _maximizeBrightnessOnPassOpen.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_THEME_MODE -> _themeMode.value = readThemeMode()
            KEY_LANGUAGE_MODE -> {
                val languageMode = readLanguageMode()
                _languageMode.value = languageMode
                applyLanguageMode(languageMode)
            }
            KEY_SHOW_EDUCATIONS -> _showEducations.value = preferences.getBoolean(KEY_SHOW_EDUCATIONS, false)
            KEY_MAXIMIZE_BRIGHTNESS_ON_PASS_OPEN -> {
                _maximizeBrightnessOnPassOpen.value =
                    preferences.getBoolean(KEY_MAXIMIZE_BRIGHTNESS_ON_PASS_OPEN, false)
            }
        }
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)
        applyLanguageMode(_languageMode.value)
    }

    override fun setThemeMode(themeMode: AppThemeMode) {
        preferences.edit()
            .putString(KEY_THEME_MODE, themeMode.name)
            .apply()
        _themeMode.value = themeMode
    }

    override fun setLanguageMode(languageMode: AppLanguageMode) {
        preferences.edit()
            .putString(KEY_LANGUAGE_MODE, languageMode.name)
            .apply()
        _languageMode.value = languageMode
        applyLanguageMode(languageMode)
    }

    override fun setShowEducations(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_SHOW_EDUCATIONS, enabled)
            .apply()
        _showEducations.value = enabled
    }

    override fun setMaximizeBrightnessOnPassOpen(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_MAXIMIZE_BRIGHTNESS_ON_PASS_OPEN, enabled)
            .apply()
        _maximizeBrightnessOnPassOpen.value = enabled
    }

    private fun readThemeMode(): AppThemeMode {
        val savedValue = preferences.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return AppThemeMode.entries.firstOrNull { themeMode -> themeMode.name == savedValue } ?: AppThemeMode.SYSTEM
    }

    private fun readLanguageMode(): AppLanguageMode {
        val savedValue = preferences.getString(KEY_LANGUAGE_MODE, AppLanguageMode.SYSTEM.name)
        return AppLanguageMode.entries.firstOrNull { languageMode -> languageMode.name == savedValue }
            ?: AppLanguageMode.SYSTEM
    }

    private fun applyLanguageMode(languageMode: AppLanguageMode) {
        localeManager.applicationLocales = if (languageMode.languageTag.isBlank()) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(languageMode.languageTag)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "wallet_settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LANGUAGE_MODE = "language_mode"
        const val KEY_SHOW_EDUCATIONS = "show_educations"
        const val KEY_MAXIMIZE_BRIGHTNESS_ON_PASS_OPEN = "maximize_brightness_on_pass_open"
    }
}
