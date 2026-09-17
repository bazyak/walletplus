package com.bazyak.walletplus.wear.di

import com.google.android.gms.wearable.Wearable
import com.bazyak.walletplus.wear.data.WearPassDatabase
import com.bazyak.walletplus.wear.data.WearPassRepository
import com.bazyak.walletplus.wear.sync.WearPhonePassOpener
import com.bazyak.walletplus.wear.sync.WearPhoneSyncRequester
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val wearDataModule = module {
    single { WearPassDatabase.getInstance(androidContext()) }
    single { get<WearPassDatabase>().wearPassDao() }
    single {
        WearPassRepository(
            context = androidContext(),
            dao = get(),
        )
    }
    single { Wearable.getDataClient(androidContext()) }
    single { Wearable.getMessageClient(androidContext()) }
    single { Wearable.getNodeClient(androidContext()) }
    single { WearPhoneSyncRequester(dataClient = get()) }
    single {
        WearPhonePassOpener(
            messageClient = get(),
            nodeClient = get(),
        )
    }
}

val wearAppModules = listOf(wearDataModule)
