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
        println("ExchangeRateWorker: 작업 시작")
        try {
            // 주말 체크 (토요일: 7, 일요일: 1)
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                println("ExchangeRateWorker: 주말이므로 환율 알림을 건너뜁니다.")
                return@withContext Result.success()
            }
            
            // 환율 변동 모니터링
            println("ExchangeRateWorker: 환율 변동 모니터링 시작")
            val changes = monitorExchangeRateUseCase().first()
            println("ExchangeRateWorker: 환율 변동 감지 완료 - ${changes.size}개 통화")
            
            // 알림 조건 체크
            println("ExchangeRateWorker: 알림 조건 체크 시작")
            val alertResults = checkAlertConditionsUseCase()
            println("ExchangeRateWorker: 알림 조건 체크 완료 - ${alertResults.size}개 알림, 트리거 필요: ${alertResults.count { it.shouldTrigger }}개")

            // 알림 발송
            println("ExchangeRateWorker: 알림 발송 시작")
            alertResults.filter { it.shouldTrigger }.forEach { result ->
                println("ExchangeRateWorker: 알림 발송 - ${result.message}")
                notificationService.showNotification(
                    title = "환율 알림",
                    message = result.message,
                    notificationId = result.alert.id.hashCode()
                )
                println("ExchangeRateWorker: 알림 발송 완료")

                // 알림 발송 후 마지막 알림 시간 업데이트
                alertRepository.updateLastTriggeredTime(
                    alertId = result.alert.id,
                    timestamp = System.currentTimeMillis()
                )
            }
            println("ExchangeRateWorker: 작업 완료")
            Result.success()
        } catch (e: Exception) {
            println("ExchangeRateWorker: 오류 발생 - ${e.message}")
            e.printStackTrace()
            Result.failure()
        }
    }
}