package net.ifmain.hwanultoktok.kmp.worker

import kotlinx.coroutines.flow.first
import net.ifmain.hwanultoktok.kmp.domain.model.AlertCheckResult
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateChange
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.domain.usecase.CheckAlertConditionsUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.MonitorExchangeRateUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.RefreshExchangeRatesUseCase

internal data class ExchangeRateWorkExecution(
    val changes: List<ExchangeRateChange>,
    val alertResults: List<AlertCheckResult>,
)

internal class ExchangeRateWorkExecutor(
    private val refreshExchangeRatesUseCase: RefreshExchangeRatesUseCase,
    private val monitorExchangeRateUseCase: MonitorExchangeRateUseCase,
    private val checkAlertConditionsUseCase: CheckAlertConditionsUseCase,
    private val notificationService: NotificationService,
    private val alertRepository: AlertRepository,
) {
    suspend fun execute(): ExchangeRateWorkExecution {
        refreshExchangeRatesUseCase().getOrThrow()

        val changes = monitorExchangeRateUseCase().first()
        val alertResults = checkAlertConditionsUseCase()

        alertResults.filter { it.shouldTrigger }.forEach { result ->
            notificationService.showNotification(
                title = "환율 알림",
                message = result.message,
                notificationId = result.alert.id.hashCode(),
            )
            alertRepository.setArmed(
                alertId = result.alert.id,
                isArmed = false,
            )
        }

        return ExchangeRateWorkExecution(
            changes = changes,
            alertResults = alertResults,
        )
    }
}
