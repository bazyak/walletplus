package com.bazyak.walletplus.settings

import androidx.annotation.StringRes
import com.bazyak.walletplus.corestrings.R

enum class AppThemeMode(@StringRes val labelResId: Int) {
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark),
    SYSTEM(R.string.theme_system),
}
