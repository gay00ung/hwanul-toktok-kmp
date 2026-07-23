package net.ifmain.hwanultoktok.kmp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Instant

internal const val DEFAULT_EXCHANGE_RATE_CHECK_HOUR = 11
internal const val DEFAULT_EXCHANGE_RATE_CHECK_MINUTE = 30

internal data class ExchangeRateAlarmRegistration(
    val triggerAtMillis: Long,
    val isExact: Boolean,
)

internal class ExchangeRateAlarmScheduler(
    context: Context,
) {
    private val applicationContext = context.applicationContext
    private val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)
    private val preferences = applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun ensureScheduled(
        hour: Int = DEFAULT_EXCHANGE_RATE_CHECK_HOUR,
        minute: Int = DEFAULT_EXCHANGE_RATE_CHECK_MINUTE,
        now: Instant = Instant.now(),
    ): ExchangeRateAlarmRegistration {
        val storedTriggerAtMillis = preferences.getLong(KEY_TRIGGER_AT_MILLIS, 0L)
        val storedAsExact = preferences.getBoolean(KEY_IS_EXACT, false)
        val storedHour = preferences.getInt(KEY_HOUR, -1)
        val storedMinute = preferences.getInt(KEY_MINUTE, -1)
        val exactAlarmAvailable = canScheduleExactAlarms()
        val hasCurrentPendingIntent = existingPendingIntent() != null

        if (storedTriggerAtMillis > now.toEpochMilli() &&
            storedAsExact == exactAlarmAvailable &&
            storedHour == hour &&
            storedMinute == minute &&
            hasCurrentPendingIntent
        ) {
            return ExchangeRateAlarmRegistration(
                triggerAtMillis = storedTriggerAtMillis,
                isExact = storedAsExact,
            )
        }

        return scheduleNext(hour, minute, now)
    }

    fun scheduleNext(
        hour: Int = DEFAULT_EXCHANGE_RATE_CHECK_HOUR,
        minute: Int = DEFAULT_EXCHANGE_RATE_CHECK_MINUTE,
        now: Instant = Instant.now(),
    ): ExchangeRateAlarmRegistration {
        val triggerAtMillis = ExchangeRateAlarmSchedule.nextWeekdayTrigger(
            now = now,
            hour = hour,
            minute = minute,
        ).toEpochMilli()
        val pendingIntent = alarmPendingIntent(hour, minute)
        val scheduledExactly = scheduleAlarm(triggerAtMillis, pendingIntent)

        preferences.edit()
            .putLong(KEY_TRIGGER_AT_MILLIS, triggerAtMillis)
            .putBoolean(KEY_IS_EXACT, scheduledExactly)
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()

        return ExchangeRateAlarmRegistration(
            triggerAtMillis = triggerAtMillis,
            isExact = scheduledExactly,
        )
    }

    fun cancel() {
        existingPendingIntent()?.let { pendingIntent ->
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        preferences.edit().clear().apply()
    }

    fun isScheduled(now: Instant = Instant.now()): Boolean =
        preferences.getLong(KEY_TRIGGER_AT_MILLIS, 0L) > now.toEpochMilli() &&
            existingPendingIntent() != null

    fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

    internal fun scheduledTriggerAtMillis(): Long =
        preferences.getLong(KEY_TRIGGER_AT_MILLIS, 0L)

    private fun scheduleAlarm(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
    ): Boolean {
        if (canScheduleExactAlarms()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
                return true
            } catch (_: SecurityException) {
                // 권한이 확인 직후 철회된 경우 아래의 일반 알람으로 대체한다.
            }
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
        return false
    }

    private fun alarmPendingIntent(hour: Int, minute: Int): PendingIntent {
        val intent = Intent(applicationContext, ExchangeRateAlarmReceiver::class.java)
            .putExtra(EXTRA_HOUR, hour)
            .putExtra(EXTRA_MINUTE, minute)

        return PendingIntent.getBroadcast(
            applicationContext,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun existingPendingIntent(): PendingIntent? =
        PendingIntent.getBroadcast(
            applicationContext,
            ALARM_REQUEST_CODE,
            Intent(applicationContext, ExchangeRateAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )

    private companion object {
        const val PREFERENCES_NAME = "exchange_rate_alarm"
        const val KEY_TRIGGER_AT_MILLIS = "trigger_at_millis"
        const val KEY_IS_EXACT = "is_exact"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        const val ALARM_REQUEST_CODE = 11_30
    }
}

internal const val EXTRA_HOUR = "exchange_rate_alarm_hour"
internal const val EXTRA_MINUTE = "exchange_rate_alarm_minute"
