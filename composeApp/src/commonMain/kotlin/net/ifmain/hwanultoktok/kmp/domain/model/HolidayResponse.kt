package net.ifmain.hwanultoktok.kmp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HolidayResponse(
    @SerialName("resultCode")
    val resultCode: Int,

    @SerialName("resultMsg")
    val resultMsg: String,

    @SerialName("Item")
    val items: List<HolidayItem>,

    @SerialName("numOfRows")
    val numOfRows: Int = 10,

    @SerialName("pageNo")
    val pageNo: Int = 1,

    @SerialName("totalCount")
    val totalCount: Int
)

@Serializable
data class HolidayItem(
    @SerialName("locdate")
    val locdate: String,

    @SerialName("seq")
    val seq: Int,

    @SerialName("isHoliday")
    val isHoliday: Boolean,

    @SerialName("dateName")
    val dateName: String,
)
