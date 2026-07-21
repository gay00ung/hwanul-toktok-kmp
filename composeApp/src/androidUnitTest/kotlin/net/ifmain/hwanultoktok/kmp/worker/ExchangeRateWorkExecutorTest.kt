package net.ifmain.hwanultoktok.kmp.worker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import net.ifmain.hwanultoktok.kmp.domain.model.AlertType
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.domain.usecase.CheckAlertConditionsUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.MonitorExchangeRateUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.RefreshExchangeRatesUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExchangeRateWorkExecutorTest {

    @Test
    fun `refreshes once before checking alerts and uses the refreshed rate`() = runTest {
        val exchangeRateRepository = FakeExchangeRateRepository(
            cachedRates = listOf(exchangeRate(baseRate = 1_300.0)),
            refreshedRates = Result.success(listOf(exchangeRate(baseRate = 1_450.0))),
        )
        val alertRepository = FakeAlertRepository(
            alerts = listOf(
                ExchangeRateAlert(
                    id = 1L,
                    currencyCode = "USD",
                    alertType = AlertType.ABOVE,
                    targetRate = 1_400.0,
                    createdAt = TEST_DATE_TIME,
                ),
            ),
        )
        val notificationService = FakeNotificationService()
        val executor = createExecutor(
            exchangeRateRepository = exchangeRateRepository,
            alertRepository = alertRepository,
            notificationService = notificationService,
        )

        val result = executor.execute()

        assertEquals(1, exchangeRateRepository.refreshCount)
        assertEquals("refresh", exchangeRateRepository.events.first())
        assertEquals(1_450.0, result.alertResults.single().currentRate)
        assertEquals(1, notificationService.notifications.size)
        assertEquals(listOf(1L to false), alertRepository.armedUpdates)
    }

    @Test
    fun `does not check stale cached rates when refresh fails`() = runTest {
        val exchangeRateRepository = FakeExchangeRateRepository(
            cachedRates = listOf(exchangeRate(baseRate = 1_450.0)),
            refreshedRates = Result.failure(IllegalStateException("network unavailable")),
        )
        val alertRepository = FakeAlertRepository(alerts = emptyList())
        val notificationService = FakeNotificationService()
        val executor = createExecutor(
            exchangeRateRepository = exchangeRateRepository,
            alertRepository = alertRepository,
            notificationService = notificationService,
        )

        val error = assertFailsWith<IllegalStateException> {
            executor.execute()
        }

        assertEquals("network unavailable", error.message)
        assertEquals(1, exchangeRateRepository.refreshCount)
        assertEquals(0, exchangeRateRepository.cachedReadCount)
        assertTrue(notificationService.notifications.isEmpty())
    }

    @Test
    fun `keeps alert armed when notification posting fails`() = runTest {
        val exchangeRateRepository = FakeExchangeRateRepository(
            cachedRates = listOf(exchangeRate(baseRate = 1_300.0)),
            refreshedRates = Result.success(listOf(exchangeRate(baseRate = 1_450.0))),
        )
        val alertRepository = FakeAlertRepository(
            alerts = listOf(
                ExchangeRateAlert(
                    id = 1L,
                    currencyCode = "USD",
                    alertType = AlertType.ABOVE,
                    targetRate = 1_400.0,
                    createdAt = TEST_DATE_TIME,
                ),
            ),
        )
        val notificationService = FakeNotificationService(
            failure = SecurityException("notification permission denied"),
        )
        val executor = createExecutor(
            exchangeRateRepository = exchangeRateRepository,
            alertRepository = alertRepository,
            notificationService = notificationService,
        )

        assertFailsWith<SecurityException> {
            executor.execute()
        }

        assertTrue(alertRepository.armedUpdates.isEmpty())
    }

    private fun createExecutor(
        exchangeRateRepository: ExchangeRateRepository,
        alertRepository: AlertRepository,
        notificationService: NotificationService,
    ): ExchangeRateWorkExecutor {
        return ExchangeRateWorkExecutor(
            refreshExchangeRatesUseCase = RefreshExchangeRatesUseCase(exchangeRateRepository),
            monitorExchangeRateUseCase = MonitorExchangeRateUseCase(exchangeRateRepository),
            checkAlertConditionsUseCase = CheckAlertConditionsUseCase(
                alertRepository = alertRepository,
                exchangeRateRepository = exchangeRateRepository,
            ),
            notificationService = notificationService,
            alertRepository = alertRepository,
        )
    }

    private class FakeExchangeRateRepository(
        private var cachedRates: List<ExchangeRate>,
        private val refreshedRates: Result<List<ExchangeRate>>,
    ) : ExchangeRateRepository {
        val events = mutableListOf<String>()
        var refreshCount = 0
            private set
        var cachedReadCount = 0
            private set
        private var previousRates: List<ExchangeRate> = emptyList()

        override suspend fun getExchangeRates(): Flow<List<ExchangeRate>> {
            events += "get"
            cachedReadCount += 1
            return flowOf(cachedRates)
        }

        override suspend fun refreshExchangeRates(): Result<List<ExchangeRate>> {
            events += "refresh"
            refreshCount += 1
            refreshedRates.onSuccess { cachedRates = it }
            return refreshedRates
        }

        override suspend fun getPreviousExchangeRates(): List<ExchangeRate> = previousRates

        override suspend fun savePreviousExchangeRates(rates: List<ExchangeRate>) {
            previousRates = rates
        }
    }

    private class FakeAlertRepository(
        private val alerts: List<ExchangeRateAlert>,
    ) : AlertRepository {
        val armedUpdates = mutableListOf<Pair<Long, Boolean>>()

        override fun getAllAlerts(): Flow<List<ExchangeRateAlert>> = flowOf(alerts)

        override suspend fun insertAlert(alert: ExchangeRateAlert) = Unit

        override suspend fun deleteAlert(alertId: Long) = Unit

        override suspend fun updateAlert(alert: ExchangeRateAlert) = Unit

        override suspend fun setArmed(alertId: Long, isArmed: Boolean) {
            armedUpdates += alertId to isArmed
        }
    }

    private class FakeNotificationService(
        private val failure: Throwable? = null,
    ) : NotificationService {
        val notifications = mutableListOf<Pair<String, String>>()

        override suspend fun showNotification(
            title: String,
            message: String,
            notificationId: Int,
        ) {
            failure?.let { throw it }
            notifications += title to message
        }

        override suspend fun requestNotificationPermission(): Boolean = true

        override suspend fun isNotificationPermissionGranted(): Boolean = true
    }

    companion object {
        private val TEST_DATE_TIME = LocalDateTime(2026, 7, 20, 12, 0)

        private fun exchangeRate(baseRate: Double): ExchangeRate {
            return ExchangeRate(
                currencyCode = "USD",
                currencyName = "미국 달러",
                currencyUnit = "USD",
                buyingRate = baseRate,
                sellingRate = baseRate,
                baseRate = baseRate,
                bookPrice = baseRate,
                timestamp = TEST_DATE_TIME,
            )
        }
    }
}
