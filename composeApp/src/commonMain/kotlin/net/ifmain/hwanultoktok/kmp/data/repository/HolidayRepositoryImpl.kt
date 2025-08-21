package net.ifmain.hwanultoktok.kmp.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
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
            val holidayItems = when (val itemElement = response.response.body.items.item) {
                is JsonObject -> {
                    val item = Json.decodeFromJsonElement<HolidayItem>(itemElement)
                    listOf(item)
                }

                is JsonArray -> {
                    Json.decodeFromJsonElement<List<HolidayItem>>(itemElement)
                }

                else -> {
                    emptyList()
                }
            }

            println("[HOLIDAY_API] 공휴일 개수 = ${holidayItems.size}개")

            holidayItems.forEach { item ->
                println("[HOLIDAY_API] ${item.dateName} - ${item.locdate} (공휴일: ${item.isHoliday})")
            }

            Result.success(holidayItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}