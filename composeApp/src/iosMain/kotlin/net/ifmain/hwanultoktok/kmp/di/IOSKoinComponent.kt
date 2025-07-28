package net.ifmain.hwanultoktok.kmp.di

import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.domain.usecase.CheckAlertConditionsUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

private object IOSKoinComponent : KoinComponent {
    
    fun initializeKoin() {
        if (KoinPlatformTools.defaultContext().getOrNull() == null) {
            startKoin {
                modules(commonModule, platformModule)
            }
        }
    }
    
    fun stopKoin() {
        org.koin.core.context.stopKoin()
    }
    
    val checkAlertConditionsUseCase: CheckAlertConditionsUseCase by inject()
    val notificationService: NotificationService by inject()
    val alertRepository: AlertRepository by inject()
}

// Top-level functions for iOS access
fun doInitKoinIOS() {
    IOSKoinComponent.initializeKoin()
}

fun getCheckAlertConditionsUseCaseIOS(): CheckAlertConditionsUseCase {
    return IOSKoinComponent.checkAlertConditionsUseCase
}

fun getNotificationServiceIOS(): NotificationService {
    return IOSKoinComponent.notificationService
}

fun getAlertRepositoryIOS(): AlertRepository {
    return IOSKoinComponent.alertRepository
}