package net.ifmain.hwanultoktok.kmp.domain.model

data class AlertCheckResult(
    val alert: ExchangeRateAlert,
    val shouldTrigger: Boolean,
    val currentRate: Double,
    val message: String
)