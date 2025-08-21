package net.ifmain.hwanultoktok.kmp.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class HolidayApiResponse(
    val response: HolidayResponseWrapper
)

@Serializable
data class HolidayResponseWrapper(
    val header: HolidayHeader,
    val body: HolidayBody
)

@Serializable
data class HolidayHeader(
    val resultCode: String,
    val resultMsg: String
)

@Serializable
data class HolidayBody(
    val items: HolidayItemsWrapper,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int
)

@Serializable
data class HolidayItemsWrapper(
    val item: JsonElement
)

@Serializable
data class HolidayItem(
    val dateKind: String,
    val dateName: String,
    val isHoliday: String, // "Y" 또는 "N"
    val locdate: Long,
    val seq: Int
)
