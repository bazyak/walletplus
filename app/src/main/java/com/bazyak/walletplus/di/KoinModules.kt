package com.bazyak.walletplus.di

import com.google.android.gms.wearable.Wearable
import com.bazyak.walletplus.data.local.PassDatabase
import com.bazyak.walletplus.data.parser.ParserRegistry
import com.bazyak.walletplus.data.repository.PassRepository
import com.bazyak.walletplus.data.repository.PassRepositoryImpl
import com.bazyak.walletplus.data.repository.WalletArchiveRepository
import com.bazyak.walletplus.education.EducationProgressRepository
import com.bazyak.walletplus.education.SharedPreferencesEducationProgressRepository
import com.bazyak.walletplus.settings.SettingsRepository
import com.bazyak.walletplus.settings.SharedPreferencesSettingsRepository
import com.bazyak.walletplus.ui.viewmodel.CustomPassBuilderViewModel
import com.bazyak.walletplus.ui.viewmodel.EducationViewModel
import com.bazyak.walletplus.ui.viewmodel.ImportStatusHolder
import com.bazyak.walletplus.ui.viewmodel.PassGridViewModel
import com.bazyak.walletplus.ui.viewmodel.PassPreviewViewModel
import com.bazyak.walletplus.ui.viewmodel.SettingsViewModel
import com.bazyak.walletplus.wear.PhoneWearSyncCoordinator
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { PassDatabase.getInstance(androidContext()) }
    single { ParserRegistry(androidContext()) }
    single {
        PassRepositoryImpl(
            passDao = get<PassDatabase>().passDao(),
            parserRegistry = get(),
            context = androidContext(),
        )
    }
    single<PassRepository> { get<PassRepositoryImpl>() }
    single<WalletArchiveRepository> { get<PassRepositoryImpl>() }
    single { Wearable.getDataClient(androidContext()) }
    single { Wearable.getNodeClient(androidContext()) }
    single {
        PhoneWearSyncCoordinator(
            context = androidContext(),
            dataClient = get(),
            nodeClient = get(),
            passRepository = get(),
        )
    }
}

val domainModule = module {
    single { ImportStatusHolder() }
    single<EducationProgressRepository> { SharedPreferencesEducationProgressRepository(androidContext()) }
    single<SettingsRepository> { SharedPreferencesSettingsRepository(androidContext()) }
}

val uiModule = module {
    viewModel { PassGridViewModel(get(), get(), get(), androidContext()) }
    viewModel { PassPreviewViewModel(get(), get(), androidContext()) }
    viewModel { CustomPassBuilderViewModel(get(), get(), androidContext()) }
    viewModel { EducationViewModel(get(), get(), androidContext()) }
    viewModel { SettingsViewModel(get(), get(), androidContext()) }
}

val appModules = listOf(dataModule, domainModule, uiModule)
