package net.ifmain.hwanultoktok.kmp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import kotlin.getValue

class ExchangeRateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    private val exchangeRateWorkExecutor: ExchangeRateWorkExecutor by inject()
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        println("ExchangeRateWorker: 작업 시작")
        try {
            // 주말 체크 (토요일: 7, 일요일: 1)
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                println("ExchangeRateWorker: 주말이므로 환율 알림을 건너뜁니다.")
                return@withContext Result.success()
            }
            
            println("ExchangeRateWorker: 최신 환율 조회 및 알림 조건 체크 시작")
            val execution = exchangeRateWorkExecutor.execute()
            println("ExchangeRateWorker: 환율 변동 감지 완료 - ${execution.changes.size}개 통화")
            println(
                "ExchangeRateWorker: 알림 조건 체크 완료 - " +
                    "${execution.alertResults.size}개 알림, " +
                    "트리거 필요: ${execution.alertResults.count { it.shouldTrigger }}개",
            )
            println("ExchangeRateWorker: 작업 완료")
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("ExchangeRateWorker: 오류 발생 - ${e.message}")
            e.printStackTrace()
            Result.failure()
        }
    }
}
