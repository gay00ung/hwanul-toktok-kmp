package net.ifmain.hwanultoktok.kmp.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.ifmain.hwanultoktok.kmp.data.remote.KoreaExImBankApi
import net.ifmain.hwanultoktok.kmp.domain.model.HolidayItem
import net.ifmain.hwanultoktok.kmp.domain.repository.HolidayRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExchangeRateRepositoryImplTest {
    @Test
    fun getExchangeRates_propagates_refresh_failure() = runTest {
        val client = HttpClient(
            MockEngine {
                throw IllegalStateException("network unavailable")
            }
        )

        try {
            val repository = ExchangeRateRepositoryImpl(
                api = KoreaExImBankApi(client),
                apiKey = "test-api-key",
                holidayRepository = SuccessfulHolidayRepository,
            )

            val error = assertFailsWith<IllegalStateException> {
                repository.getExchangeRates().first()
            }

            assertEquals("network unavailable", error.message)
        } finally {
            client.close()
        }
    }
}

private object SuccessfulHolidayRepository : HolidayRepository {
    override suspend fun getHolidays(year: Int, month: Int): Result<List<HolidayItem>> =
        Result.success(emptyList())
}
