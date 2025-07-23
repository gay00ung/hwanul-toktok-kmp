package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository

class ManageAlertUseCase(
    private val repository: AlertRepository
) {
    suspend fun addAlert(alert: ExchangeRateAlert) {
        repository.insertAlert(alert)
    }
    
    suspend fun deleteAlert(alertId: Long) {
        repository.deleteAlert(alertId)
    }
    
    suspend fun updateAlert(alert: ExchangeRateAlert) {
        repository.updateAlert(alert)
    }
}