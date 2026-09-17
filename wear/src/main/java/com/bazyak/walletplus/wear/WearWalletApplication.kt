package com.bazyak.walletplus.wear

import android.app.Application
import com.bazyak.walletplus.wear.di.wearAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WearWalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WearWalletApplication)
            modules(wearAppModules)
        }
    }
}
