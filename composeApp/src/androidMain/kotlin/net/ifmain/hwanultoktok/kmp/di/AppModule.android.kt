package net.ifmain.hwanultoktok.kmp.di

import net.ifmain.hwanultoktok.kmp.BuildConfig
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory(get()) }
    single { BuildConfig.KOREAEXIM_API_KEY }
}