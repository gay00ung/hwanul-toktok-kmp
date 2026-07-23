package net.ifmain.hwanultoktok.kmp.alarm

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ExchangeRateAlarmScheduleTest {
    private val koreaZone = ZoneId.of("Asia/Seoul")

    @Test
    fun before_target_time_schedules_same_weekday() {
        val now = koreaTime(2026, 7, 20, 10, 0)

        val next = ExchangeRateAlarmSchedule.nextWeekdayTrigger(
            now = now,
            hour = 11,
            minute = 30,
        )

        assertEquals(koreaTime(2026, 7, 20, 11, 30), next)
    }

    @Test
    fun at_target_time_schedules_next_weekday() {
        val now = koreaTime(2026, 7, 20, 11, 30)

        val next = ExchangeRateAlarmSchedule.nextWeekdayTrigger(
            now = now,
            hour = 11,
            minute = 30,
        )

        assertEquals(koreaTime(2026, 7, 21, 11, 30), next)
    }

    @Test
    fun after_friday_target_time_skips_weekend() {
        val now = koreaTime(2026, 7, 31, 12, 0)

        val next = ExchangeRateAlarmSchedule.nextWeekdayTrigger(
            now = now,
            hour = 11,
            minute = 30,
        )

        assertEquals(koreaTime(2026, 8, 3, 11, 30), next)
    }

    @Test
    fun weekend_schedules_next_monday() {
        val now = koreaTime(2026, 8, 1, 10, 0)

        val next = ExchangeRateAlarmSchedule.nextWeekdayTrigger(
            now = now,
            hour = 11,
            minute = 30,
        )

        assertEquals(koreaTime(2026, 8, 3, 11, 30), next)
    }

    private fun koreaTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Instant = ZonedDateTime.of(
        year,
        month,
        day,
        hour,
        minute,
        0,
        0,
        koreaZone,
    ).toInstant()
}
