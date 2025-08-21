package net.ifmain.hwanultoktok.kmp.di

import net.ifmain.hwanultoktok.kmp.BuildConfig
import net.ifmain.hwanultoktok.kmp.data.repository.AndroidBackgroundTaskRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.service.AndroidNotificationService
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory(get()) }
    single { BuildConfig.KOREAEXIM_API_KEY }
    single(named("holidayApiKey")) { BuildConfig.KOREA_HOLIDAY_API_KEY_DECODING }
    single<BackgroundTaskRepository> { AndroidBackgroundTaskRepository(get()) }
    single<NotificationService> { AndroidNotificationService(get()) }
}