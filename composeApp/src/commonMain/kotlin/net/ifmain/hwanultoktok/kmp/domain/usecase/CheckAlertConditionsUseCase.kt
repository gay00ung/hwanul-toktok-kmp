package net.ifmain.hwanultoktok.kmp.domain.usecase

import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.flow.first
import net.ifmain.hwanultoktok.kmp.domain.model.AlertCheckResult
import net.ifmain.hwanultoktok.kmp.domain.model.AlertType
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
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
            
            // 중복 알림 방지를 위해 마지막 알림 시간 체크
            val lastTriggeredTime = alertRepository.getLastTriggeredTime(alert.id)
            val now = getTimeMillis()
            val cooldownPeriod = 60 * 60 * 1000L // 1시간
            
            val shouldSend = shouldTrigger && 
                (lastTriggeredTime == null || now - lastTriggeredTime > cooldownPeriod)
            
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