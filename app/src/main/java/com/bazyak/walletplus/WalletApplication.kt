package com.bazyak.walletplus

import android.app.Application
import com.bazyak.walletplus.di.appModules
import com.bazyak.walletplus.wear.PhoneWearSyncCoordinator
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WalletApplication : Application() {
    private lateinit var wearSyncCoordinator: PhoneWearSyncCoordinator

    override fun onCreate() {
        super.onCreate()
        val koinApplication = startKoin {
            androidContext(this@WalletApplication)
            modules(appModules)
        }

        wearSyncCoordinator = koinApplication.koin.get()
        wearSyncCoordinator.start()
    }
}
