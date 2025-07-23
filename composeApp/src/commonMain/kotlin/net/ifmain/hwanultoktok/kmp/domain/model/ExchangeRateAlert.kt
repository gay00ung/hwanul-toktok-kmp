package net.ifmain.hwanultoktok.kmp.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateAlert(
    val id: Long = 0,
    val currencyCode: String,
    val alertType: AlertType,
    val targetRate: Double,
    val isEnabled: Boolean = true,
    val createdAt: LocalDateTime
)

enum class AlertType {
    ABOVE, BELOW
}