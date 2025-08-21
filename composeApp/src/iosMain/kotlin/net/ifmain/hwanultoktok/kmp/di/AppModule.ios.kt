package net.ifmain.hwanultoktok.kmp.di

import net.ifmain.hwanultoktok.kmp.data.repository.IosBackgroundTaskRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.platform.ApiKeyProvider
import net.ifmain.hwanultoktok.kmp.service.IosNotificationService
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory() }
    single { ApiKeyProvider.getApiKey() }
    single(named("holidayApiKey")) { ApiKeyProvider.getHolidayApiKey() }
    single<BackgroundTaskRepository> { 
        IosBackgroundTaskRepository(
            checkAlertConditionsUseCase = get(),
            notificationService = get(),
            alertRepository = get()
        )
    }
    single<NotificationService> { IosNotificationService() }

}