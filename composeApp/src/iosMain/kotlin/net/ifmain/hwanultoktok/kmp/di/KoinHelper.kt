package net.ifmain.hwanultoktok.kmp.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule)
    }
}

// Helper function for iOS
fun doInitKoin() = initKoin()