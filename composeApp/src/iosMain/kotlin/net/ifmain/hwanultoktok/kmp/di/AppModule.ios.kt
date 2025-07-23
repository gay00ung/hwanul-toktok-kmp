package net.ifmain.hwanultoktok.kmp.di

import net.ifmain.hwanultoktok.kmp.platform.ApiKeyProvider
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory() }
    single { ApiKeyProvider.getApiKey() }
}