package net.ifmain.hwanultoktok.kmp.domain.repository

import kotlinx.coroutines.flow.Flow
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert

interface AlertRepository {
    fun getAllAlerts(): Flow<List<ExchangeRateAlert>>
    suspend fun insertAlert(alert: ExchangeRateAlert)
    suspend fun deleteAlert(alertId: Long)
    suspend fun updateAlert(alert: ExchangeRateAlert)
}