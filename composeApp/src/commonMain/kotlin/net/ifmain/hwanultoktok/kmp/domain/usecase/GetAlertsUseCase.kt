package net.ifmain.hwanultoktok.kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository

class GetAlertsUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(): Flow<List<ExchangeRateAlert>> {
        return repository.getAllAlerts()
    }
}