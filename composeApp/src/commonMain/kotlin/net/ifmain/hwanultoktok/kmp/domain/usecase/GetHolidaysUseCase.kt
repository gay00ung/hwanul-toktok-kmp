package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.model.HolidayItem
import net.ifmain.hwanultoktok.kmp.domain.repository.HolidayRepository

class GetHolidaysUseCase(
    private val holidayRepository: HolidayRepository
) {
    suspend operator fun invoke(year: Int, month: Int): Result<List<HolidayItem>> {
        return holidayRepository.getHolidays(year, month)
    }
}