package net.ifmain.hwanultoktok.kmp.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRate(
    val currencyCode: String,
    val currencyName: String,
    val currencyUnit: String,
    val buyingRate: Double,
    val sellingRate: Double,
    val baseRate: Double,
    val bookPrice: Double,
    val timestamp: LocalDateTime
)