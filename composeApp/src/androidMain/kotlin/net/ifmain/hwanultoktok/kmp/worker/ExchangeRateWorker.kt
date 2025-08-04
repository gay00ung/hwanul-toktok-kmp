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
import java.util.Calendar
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
            // 주말 체크 (토요일: 7, 일요일: 1)
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                println("주말이므로 환율 알림을 건너뜁니다.")
                return@withContext Result.success()
            }
            
            // 환율 변동 모니터링
            val changes = monitorExchangeRateUseCase().first()
            
            // 알림 조건 체크
            val alertResults = checkAlertConditionsUseCase()

            // 알림 발송
            alertResults.filter { it.shouldTrigger }.forEach { result ->
                notificationService.showNotification(
                    title = "환율 알림",
                    message = result.message,
                    notificationId = result.alert.id.hashCode()
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