package net.ifmain.hwanultoktok.kmp.util

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import net.ifmain.hwanultoktok.kmp.domain.model.HolidayItem
import net.ifmain.hwanultoktok.kmp.domain.repository.HolidayRepository
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetHolidaysUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DateTimeUtilsTest {
    @Test
    fun holiday_lookup_timeout_falls_back_to_weekday_calculation() = runTest {
        val updateTime = LocalDateTime(2026, 7, 27, 12, 0)
        val startTime = testScheduler.currentTime

        val dataDate = getDataBaseDateWithFallback(
            updateTime = updateTime,
            getHolidaysUseCase = GetHolidaysUseCase(HangingHolidayRepository),
        )

        assertEquals(LocalDate(2026, 7, 27), dataDate)
        assertEquals(
            HOLIDAY_LOOKUP_TIMEOUT_MILLIS,
            testScheduler.currentTime - startTime,
        )
    }

    @Test
    fun caller_timeout_is_not_swallowed_by_holiday_fallback() = runTest {
        var callerTimedOut = false

        try {
            withTimeout(1_000L) {
                getDataBaseDateWithFallback(
                    updateTime = LocalDateTime(2026, 7, 27, 12, 0),
                    getHolidaysUseCase = GetHolidaysUseCase(HangingHolidayRepository),
                )
            }
        } catch (_: TimeoutCancellationException) {
            callerTimedOut = true
        }

        assertTrue(callerTimedOut)
    }
}

private object HangingHolidayRepository : HolidayRepository {
    override suspend fun getHolidays(
        year: Int,
        month: Int,
    ): Result<List<HolidayItem>> = awaitCancellation()
}
