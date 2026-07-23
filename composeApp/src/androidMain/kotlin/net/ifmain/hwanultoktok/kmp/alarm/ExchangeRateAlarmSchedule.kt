package net.ifmain.hwanultoktok.kmp.alarm

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal object ExchangeRateAlarmSchedule {
    private val koreaZone: ZoneId = ZoneId.of("Asia/Seoul")

    fun nextWeekdayTrigger(
        now: Instant,
        hour: Int,
        minute: Int,
    ): Instant {
        val targetTime = LocalTime.of(hour, minute)
        var candidate = ZonedDateTime.of(
            now.atZone(koreaZone).toLocalDate(),
            targetTime,
            koreaZone,
        )

        if (!candidate.toInstant().isAfter(now)) {
            candidate = candidate.plusDays(1)
        }

        while (candidate.dayOfWeek == DayOfWeek.SATURDAY ||
            candidate.dayOfWeek == DayOfWeek.SUNDAY
        ) {
            candidate = candidate.plusDays(1)
        }

        return candidate.toInstant()
    }
}
