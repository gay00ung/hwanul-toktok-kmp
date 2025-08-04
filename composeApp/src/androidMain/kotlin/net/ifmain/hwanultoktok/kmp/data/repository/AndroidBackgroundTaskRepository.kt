package net.ifmain.hwanultoktok.kmp.data.repository

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository
import net.ifmain.hwanultoktok.kmp.worker.ExchangeRateWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AndroidBackgroundTaskRepository(
    private val context: Context
) : BackgroundTaskRepository {
    companion object {
        const val EXCHANGE_RATE_WORK_TAG = "exchange_rate_check"
    }

    override suspend fun scheduleExchangeRateCheck(hour: Int, minute: Int) {
        println("AndroidBackgroundTaskRepository: scheduleExchangeRateCheck 호출 - hour: $hour, minute: $minute")
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ExchangeRateWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(EXCHANGE_RATE_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EXCHANGE_RATE_WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
        println("AndroidBackgroundTaskRepository: WorkManager 작업 예약 완료 - 초기 대기 시간: ${initialDelay / 1000 / 60}분")
    }

    override suspend fun cancelExchangeRateCheck() {
        WorkManager.getInstance(context).cancelUniqueWork(EXCHANGE_RATE_WORK_TAG)
    }

    override suspend fun isExchangeRateCheckScheduled(): Boolean = withContext(Dispatchers.IO) {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(EXCHANGE_RATE_WORK_TAG)
            .get()

        workInfos.isNotEmpty() && workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }

    override suspend fun executeExchangeRateCheck() {
        println("AndroidBackgroundTaskRepository: executeExchangeRateCheck 호출")
        val workRequest = OneTimeWorkRequestBuilder<ExchangeRateWorker>()
            .addTag("immediate_check")
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
        println("AndroidBackgroundTaskRepository: 즉시 실행 작업 예약 완료")
    }

}