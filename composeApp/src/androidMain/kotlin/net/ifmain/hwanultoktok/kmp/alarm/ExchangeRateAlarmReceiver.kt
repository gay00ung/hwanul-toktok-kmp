package net.ifmain.hwanultoktok.kmp.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ExchangeRateAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra(
            EXTRA_HOUR,
            DEFAULT_EXCHANGE_RATE_CHECK_HOUR,
        )
        val minute = intent.getIntExtra(
            EXTRA_MINUTE,
            DEFAULT_EXCHANGE_RATE_CHECK_MINUTE,
        )

        ExchangeRateAlarmScheduler(context).scheduleNext(hour, minute)
        ExchangeRateWorkScheduler.enqueue(context)
    }
}

class ExchangeRateAlarmRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            -> Unit

            else -> return
        }

        ExchangeRateAlarmScheduler(context).scheduleNext()
    }
}
