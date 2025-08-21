package net.ifmain.hwanultoktok.kmp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HolidayDto(
    @SerialName("solYear")
    val year: Int,
    @SerialName("solMonth")
    val month: Int? = null,
    @SerialName("ServiceKey")
    val serviceKey: String,
    @SerialName("_type")
    val type: String? = "json",
    @SerialName("numOfRows")
    val numOfRows: Int? = 10,
)
