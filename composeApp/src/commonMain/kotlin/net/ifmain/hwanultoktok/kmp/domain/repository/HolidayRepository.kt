package net.ifmain.hwanultoktok.kmp.domain.repository

import net.ifmain.hwanultoktok.kmp.domain.model.HolidayItem

interface HolidayRepository {
    suspend fun getHolidays(year: Int, month: Int): Result<List<HolidayItem>>
}