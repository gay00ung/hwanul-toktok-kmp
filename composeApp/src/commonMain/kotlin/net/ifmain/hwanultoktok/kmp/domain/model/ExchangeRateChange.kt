package net.ifmain.hwanultoktok.kmp.domain.model

data class ExchangeRateChange(
    val currencyCode: String,
    val previousRate: Double,
    val currentRate: Double,
    val changeAmount: Double,
    val changePercentage: Double,
    val timestamp: Long
)
 