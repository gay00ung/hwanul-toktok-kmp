package net.ifmain.hwanultoktok.kmp.alarm

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import net.ifmain.hwanultoktok.kmp.worker.ExchangeRateWorker

internal object ExchangeRateWorkScheduler {
    const val WORK_NAME = "exchange_rate_check"
    const val WORK_TAG = "exchange_rate_check"

    fun enqueue(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<ExchangeRateWorker>()
            .setConstraints(constraints)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
    }
}
