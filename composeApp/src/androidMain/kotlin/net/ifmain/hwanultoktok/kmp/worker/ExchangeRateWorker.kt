package net.ifmain.hwanultoktok.kmp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import net.ifmain.hwanultoktok.kmp.domain.usecase.CheckAlertConditionsUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.MonitorExchangeRateUseCase
import kotlin.getValue

class ExchangeRateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    private val checkAlertConditionsUseCase: CheckAlertConditionsUseCase by inject()
    private val monitorExchangeRateUseCase: MonitorExchangeRateUseCase by inject()
    private val notificationService: NotificationService by inject()
    private val alertRepository: AlertRepository by inject()
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 환율 변동 모니터링
            val changes = monitorExchangeRateUseCase().first()
            
            // 알림 조건 체크
            val alertResults = checkAlertConditionsUseCase()

            // 알림 발송
            alertResults.filter { it.shouldTrigger }.forEach { result ->
                notificationService.showNotification(
                    title = "환율 알림",
                    message = result.message
                )

                // 알림 발송 후 마지막 알림 시간 업데이트
                alertRepository.updateLastTriggeredTime(
                    alertId = result.alert.id,
                    timestamp = System.currentTimeMillis()
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}