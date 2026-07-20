package net.ifmain.hwanultoktok.kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import net.ifmain.hwanultoktok.kmp.domain.model.AlertType
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckAlertConditionsUseCaseTest {
    @Test
    fun armed_alert_triggers_once_while_condition_remains_met() = runTest {
        val alertRepository = FakeAlertRepository(aboveAlert(isArmed = true))
        val exchangeRateRepository = FakeAlertExchangeRateRepository(jpyRate(baseRate = 910.0))
        val useCase = CheckAlertConditionsUseCase(alertRepository, exchangeRateRepository)

        val firstCheck = useCase().single()
        assertTrue(firstCheck.shouldTrigger)

        alertRepository.setArmed(firstCheck.alert.id, isArmed = false)
        val secondCheck = useCase().single()

        assertFalse(secondCheck.shouldTrigger)
    }

    @Test
    fun disarmed_alert_rearms_after_condition_clears_and_triggers_on_reentry() = runTest {
        val alertRepository = FakeAlertRepository(aboveAlert(isArmed = false))
        val exchangeRateRepository = FakeAlertExchangeRateRepository(jpyRate(baseRate = 890.0))
        val useCase = CheckAlertConditionsUseCase(alertRepository, exchangeRateRepository)

        val clearedCheck = useCase().single()
        assertFalse(clearedCheck.shouldTrigger)
        assertTrue(alertRepository.currentAlert.isArmed)

        exchangeRateRepository.currentRate = jpyRate(baseRate = 910.0)
        val reentryCheck = useCase().single()

        assertTrue(reentryCheck.shouldTrigger)
    }

    @Test
    fun alert_message_uses_official_currency_unit() = runTest {
        val alertRepository = FakeAlertRepository(aboveAlert(isArmed = true))
        val exchangeRateRepository = FakeAlertExchangeRateRepository(jpyRate(baseRate = 910.0))

        val result = CheckAlertConditionsUseCase(alertRepository, exchangeRateRepository)().single()

        assertContains(result.message, "JPY(100)")
    }
}

private fun aboveAlert(isArmed: Boolean): ExchangeRateAlert {
    return ExchangeRateAlert(
        id = 1L,
        currencyCode = "JPY",
        alertType = AlertType.ABOVE,
        targetRate = 900.0,
        createdAt = LocalDateTime(2026, 7, 20, 12, 0),
        isArmed = isArmed,
    )
}

private fun jpyRate(baseRate: Double): ExchangeRate {
    return ExchangeRate(
        currencyCode = "JPY",
        currencyName = "일본 옌",
        currencyUnit = "JPY(100)",
        buyingRate = baseRate - 5.0,
        sellingRate = baseRate + 5.0,
        baseRate = baseRate,
        bookPrice = baseRate,
        timestamp = LocalDateTime(2026, 7, 20, 12, 0),
    )
}

private class FakeAlertRepository(initialAlert: ExchangeRateAlert) : AlertRepository {
    var currentAlert = initialAlert
        private set

    override fun getAllAlerts(): Flow<List<ExchangeRateAlert>> = flowOf(listOf(currentAlert))

    override suspend fun insertAlert(alert: ExchangeRateAlert) {
        currentAlert = alert
    }

    override suspend fun deleteAlert(alertId: Long) = Unit

    override suspend fun updateAlert(alert: ExchangeRateAlert) {
        currentAlert = alert
    }

    override suspend fun setArmed(alertId: Long, isArmed: Boolean) {
        currentAlert = currentAlert.copy(isArmed = isArmed)
    }
}

private class FakeAlertExchangeRateRepository(
    var currentRate: ExchangeRate,
) : ExchangeRateRepository {
    override suspend fun getExchangeRates(): Flow<List<ExchangeRate>> = flowOf(listOf(currentRate))

    override suspend fun refreshExchangeRates(): Result<List<ExchangeRate>> =
        Result.success(listOf(currentRate))

    override suspend fun getPreviousExchangeRates(): List<ExchangeRate> = emptyList()

    override suspend fun savePreviousExchangeRates(rates: List<ExchangeRate>) = Unit
}
