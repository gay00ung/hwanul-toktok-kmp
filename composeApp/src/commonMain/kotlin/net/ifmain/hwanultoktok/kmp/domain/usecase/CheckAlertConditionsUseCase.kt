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
            
            val currentRate = exchangeRates.find { 
                it.currencyCode == alert.currencyCode 
            }?.baseRate ?: return@mapNotNull null
            
            val shouldTrigger = when (alert.alertType) {
                AlertType.ABOVE -> currentRate >= alert.targetRate
                AlertType.BELOW -> currentRate <= alert.targetRate
            }
            
            // 매일 한 번만 체크하므로 중복 알림 방지 불필요
            val shouldSend = shouldTrigger
            
            AlertCheckResult(
                alert = alert,
                shouldTrigger = shouldSend,
                currentRate = currentRate,
                message = if (shouldSend) {
                    "${alert.currencyCode} 환율이 ${alert.targetRate}에 도달했습니다. 현재: $currentRate"
                } else ""
            )
        }
    }
}