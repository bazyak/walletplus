package com.bazyak.walletplus.wear.data

import com.bazyak.walletplus.wearsync.WearPassSnapshot

data class CachedWearPass(val snapshot: WearPassSnapshot, val iconPath: String?, val logoPath: String?)
