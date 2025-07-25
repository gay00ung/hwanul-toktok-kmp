package net.ifmain.hwanultoktok.kmp.di

import net.ifmain.hwanultoktok.kmp.data.remote.KoreaExImBankApi
import net.ifmain.hwanultoktok.kmp.data.repository.AlertRepositoryImpl
import net.ifmain.hwanultoktok.kmp.data.repository.ExchangeRateRepositoryImpl
import net.ifmain.hwanultoktok.kmp.data.repository.FavoriteRepositoryImpl
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.FavoriteRepository
import net.ifmain.hwanultoktok.kmp.domain.usecase.CheckAlertConditionsUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.CheckIsFavoriteUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetAlertsUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetFavoritesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ManageAlertUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.MonitorExchangeRateUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.RefreshExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ScheduleExchangeRateCheckUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ToggleFavoriteUseCase
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.AlertViewModel
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.ExchangeRateViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val commonModule = module {
    single { createHttpClient() }
    single { KoreaExImBankApi(get()) }
    single { createDatabase(get()) }
    single { MonitorExchangeRateUseCase(get()) }
    single { CheckAlertConditionsUseCase(get(), get()) }
    single { ScheduleExchangeRateCheckUseCase(get()) }


    single<ExchangeRateRepository> { ExchangeRateRepositoryImpl(get(), get()) }
    single<AlertRepository> { AlertRepositoryImpl(get()) }
    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }

    factoryOf(::GetExchangeRatesUseCase)
    factoryOf(::RefreshExchangeRatesUseCase)
    factoryOf(::GetAlertsUseCase)
    factoryOf(::ManageAlertUseCase)
    factoryOf(::GetFavoritesUseCase)
    factoryOf(::ToggleFavoriteUseCase)
    factoryOf(::CheckIsFavoriteUseCase)
    
    factoryOf(::ExchangeRateViewModel)
    factoryOf(::AlertViewModel)
}

expect val platformModule: org.koin.core.module.Module