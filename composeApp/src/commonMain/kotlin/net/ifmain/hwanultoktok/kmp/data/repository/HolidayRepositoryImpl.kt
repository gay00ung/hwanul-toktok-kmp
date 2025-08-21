package net.ifmain.hwanultoktok.kmp.data.repository

import net.ifmain.hwanultoktok.kmp.data.remote.KoreaHolidayApi
import net.ifmain.hwanultoktok.kmp.domain.model.HolidayItem
import net.ifmain.hwanultoktok.kmp.domain.repository.HolidayRepository

class HolidayRepositoryImpl(
    private val holidayApi: KoreaHolidayApi,
    private val holidayApiKey: String
) : HolidayRepository {

    override suspend fun getHolidays(year: Int, month: Int): Result<List<HolidayItem>> {
        return try {
            val response = holidayApi.getHolidays(holidayApiKey, year, month)
            Result.success(response.items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}