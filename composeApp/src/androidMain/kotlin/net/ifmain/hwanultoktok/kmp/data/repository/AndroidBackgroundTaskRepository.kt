package net.ifmain.hwanultoktok.kmp.data.repository

import android.content.Context
import net.ifmain.hwanultoktok.kmp.alarm.ExchangeRateAlarmScheduler
import net.ifmain.hwanultoktok.kmp.alarm.ExchangeRateWorkScheduler
import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository

class AndroidBackgroundTaskRepository(
    private val context: Context
) : BackgroundTaskRepository {
    companion object {
        const val EXCHANGE_RATE_WORK_TAG = ExchangeRateWorkScheduler.WORK_TAG
    }

    override suspend fun scheduleExchangeRateCheck(hour: Int, minute: Int) {
        ExchangeRateAlarmScheduler(context).scheduleNext(hour, minute)
    }

    override suspend fun cancelExchangeRateCheck() {
        ExchangeRateAlarmScheduler(context).cancel()
    }

    override suspend fun isExchangeRateCheckScheduled(): Boolean =
        ExchangeRateAlarmScheduler(context).isScheduled()

    override suspend fun executeExchangeRateCheck() {
        ExchangeRateWorkScheduler.enqueue(context)
    }
}
