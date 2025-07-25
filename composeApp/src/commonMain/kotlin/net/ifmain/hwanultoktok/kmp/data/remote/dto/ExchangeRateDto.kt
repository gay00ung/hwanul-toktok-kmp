package net.ifmain.hwanultoktok.kmp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateDto(
    @SerialName("result")
    val result: Int? = null,
    @SerialName("cur_unit")
    val currencyUnit: String,
    @SerialName("cur_nm")
    val currencyName: String,
    @SerialName("ttb")
    val buyingRate: String? = null,
    @SerialName("tts") 
    val sellingRate: String? = null,
    @SerialName("deal_bas_r")
    val baseRate: String? = null,
    @SerialName("bkpr")
    val bookPrice: String? = null,
    @SerialName("yy_efee_r")
    val yearFeeRate: String? = null,
    @SerialName("ten_dd_efee_r")
    val tenDayFeeRate: String? = null,
    @SerialName("kftc_bkpr")
    val kftcBankRate: String? = null,
    @SerialName("kftc_deal_bas_r")
    val kftcBasicRate: String? = null
)