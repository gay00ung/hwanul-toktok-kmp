package net.ifmain.hwanultoktok.kmp.di

import net.ifmain.hwanultoktok.kmp.data.repository.IosBackgroundTaskRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.platform.ApiKeyProvider
import net.ifmain.hwanultoktok.kmp.service.IosNotificationService
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory() }
    single { ApiKeyProvider.getApiKey() }
    single<BackgroundTaskRepository> { IosBackgroundTaskRepository() }
    single<NotificationService> { IosNotificationService() }

}