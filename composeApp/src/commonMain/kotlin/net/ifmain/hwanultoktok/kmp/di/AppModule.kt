package net.ifmain.hwanultoktok.kmp.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import net.ifmain.hwanultoktok.kmp.data.remote.KoreaExImBankApi
import net.ifmain.hwanultoktok.kmp.data.repository.AlertRepositoryImpl
import net.ifmain.hwanultoktok.kmp.data.repository.ExchangeRateRepositoryImpl
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetAlertsUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ManageAlertUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.RefreshExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.ExchangeRateViewModel
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.AlertViewModel

val commonModule = module {
    single { createHttpClient() }
    single { KoreaExImBankApi(get()) }
    single { createDatabase(get()) }
    
    single<ExchangeRateRepository> { ExchangeRateRepositoryImpl(get(), get()) }
    single<AlertRepository> { AlertRepositoryImpl(get()) }
    
    factoryOf(::GetExchangeRatesUseCase)
    factoryOf(::RefreshExchangeRatesUseCase)
    factoryOf(::GetAlertsUseCase)
    factoryOf(::ManageAlertUseCase)
    
    factoryOf(::ExchangeRateViewModel)
    factoryOf(::AlertViewModel)
}

expect val platformModule: org.koin.core.module.Module