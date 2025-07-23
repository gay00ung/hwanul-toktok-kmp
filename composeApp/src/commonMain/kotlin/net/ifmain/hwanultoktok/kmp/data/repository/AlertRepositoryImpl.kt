package net.ifmain.hwanultoktok.kmp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import net.ifmain.hwanultoktok.kmp.database.HwanulDatabase
import net.ifmain.hwanultoktok.kmp.domain.model.AlertType
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AlertRepositoryImpl(
    private val database: HwanulDatabase
) : AlertRepository {

    override fun getAllAlerts(): Flow<List<ExchangeRateAlert>> {
        return database.exchangeRateAlertQueries.getAllAlerts()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { alerts ->
                alerts.map { alert ->
                    ExchangeRateAlert(
                        id = alert.id,
                        currencyCode = alert.currencyCode,
                        alertType = AlertType.valueOf(alert.alertType),
                        targetRate = alert.targetRate,
                        isEnabled = alert.isEnabled == 1L,
                        createdAt = LocalDateTime.parse(alert.createdAt)
                    )
                }
            }
    }

    override suspend fun insertAlert(alert: ExchangeRateAlert) {
        database.exchangeRateAlertQueries.insertAlert(
            currencyCode = alert.currencyCode,
            alertType = alert.alertType.name,
            targetRate = alert.targetRate,
            isEnabled = if (alert.isEnabled) 1 else 0,
            createdAt = alert.createdAt.toString()
        )
    }

    override suspend fun deleteAlert(alertId: Long) {
        database.exchangeRateAlertQueries.deleteAlert(alertId)
    }

    override suspend fun updateAlert(alert: ExchangeRateAlert) {
        database.exchangeRateAlertQueries.updateAlert(
            currencyCode = alert.currencyCode,
            alertType = alert.alertType.name,
            targetRate = alert.targetRate,
            isEnabled = if (alert.isEnabled) 1 else 0,
            createdAt = alert.createdAt.toString(),
            id = alert.id
        )
    }
}