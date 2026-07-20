package net.ifmain.hwanultoktok.kmp.domain.usecase

import kotlinx.coroutines.flow.first
import net.ifmain.hwanultoktok.kmp.domain.model.AlertCheckResult
import net.ifmain.hwanultoktok.kmp.domain.model.AlertType
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository

class CheckAlertConditionsUseCase(
    private val alertRepository: AlertRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) {
    suspend operator fun invoke(): List<AlertCheckResult> {
        val alerts = alertRepository.getAllAlerts().first()
        val exchangeRates = exchangeRateRepository.getExchangeRates().first()
        
        return alerts.mapNotNull { alert ->
            if (!alert.isEnabled) return@mapNotNull null
            
            val exchangeRate = exchangeRates.find {
                it.currencyCode == alert.currencyCode 
            } ?: return@mapNotNull null
            val currentRate = exchangeRate.baseRate
            
            val shouldTrigger = when (alert.alertType) {
                AlertType.ABOVE -> currentRate >= alert.targetRate
                AlertType.BELOW -> currentRate <= alert.targetRate
            }
            
            if (!shouldTrigger && !alert.isArmed) {
                alertRepository.setArmed(alert.id, isArmed = true)
            }

            val shouldSend = shouldTrigger && alert.isArmed
            
            AlertCheckResult(
                alert = alert,
                shouldTrigger = shouldSend,
                currentRate = currentRate,
                message = if (shouldSend) {
                    "${exchangeRate.currencyUnit} 환율이 ${alert.targetRate}에 도달했습니다. 현재: $currentRate"
                } else ""
            )
        }
    }
}
