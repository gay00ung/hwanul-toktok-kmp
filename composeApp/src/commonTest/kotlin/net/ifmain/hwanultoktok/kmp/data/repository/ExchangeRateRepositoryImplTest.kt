package net.ifmain.hwanultoktok.kmp.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import net.ifmain.hwanultoktok.kmp.data.remote.KoreaExImBankApi
import net.ifmain.hwanultoktok.kmp.di.createHttpClient
import net.ifmain.hwanultoktok.kmp.domain.model.HolidayItem
import net.ifmain.hwanultoktok.kmp.domain.repository.HolidayRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
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

    @Test
    fun refresh_does_not_wait_for_holiday_lookup_when_weekday_response_has_rates() = runTest {
        val client = jsonClient(VALID_EXCHANGE_RATE_RESPONSE)

        try {
            val repository = ExchangeRateRepositoryImpl(
                api = KoreaExImBankApi(client),
                apiKey = "test-api-key",
                holidayRepository = HangingHolidayRepository,
            )

            val result = repository.refreshExchangeRates()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
        } finally {
            client.close()
        }
    }

    @Test
    fun refresh_retries_holiday_adjusted_date_when_weekday_response_is_empty() = runTest {
        val requestedDates = mutableListOf<String>()
        val client = createHttpClient(
            platformClient = HttpClient(
                MockEngine { request ->
                    val searchDate = request.url.parameters["searchdate"].orEmpty()
                    requestedDates += searchDate
                    respond(
                        content = if (searchDate == "20260717") {
                            "[]"
                        } else {
                            VALID_EXCHANGE_RATE_RESPONSE
                        },
                        headers = JSON_HEADERS,
                    )
                },
            ),
            enableNetworkLogging = false,
        )

        try {
            val repository = ExchangeRateRepositoryImpl(
                api = KoreaExImBankApi(client),
                apiKey = "test-api-key",
                holidayRepository = July17HolidayRepository,
                nowProvider = { LocalDateTime(2026, 7, 17, 12, 0) },
            )

            val result = repository.refreshExchangeRates()

            assertTrue(result.isSuccess)
            assertEquals(listOf("20260717", "20260716"), requestedDates)
        } finally {
            client.close()
        }
    }

    @Test
    fun refresh_treats_empty_api_response_as_failure() = runTest {
        val client = jsonClient("[]")

        try {
            val repository = ExchangeRateRepositoryImpl(
                api = KoreaExImBankApi(client),
                apiKey = "test-api-key",
                holidayRepository = SuccessfulHolidayRepository,
            )

            val result = repository.refreshExchangeRates()

            assertTrue(result.isFailure)
            assertEquals(
                "조회된 환율 정보가 없습니다.",
                result.exceptionOrNull()?.message,
            )
        } finally {
            client.close()
        }
    }
}

private object SuccessfulHolidayRepository : HolidayRepository {
    override suspend fun getHolidays(year: Int, month: Int): Result<List<HolidayItem>> =
        Result.success(emptyList())
}

private object HangingHolidayRepository : HolidayRepository {
    override suspend fun getHolidays(
        year: Int,
        month: Int,
    ): Result<List<HolidayItem>> = awaitCancellation()
}

private object July17HolidayRepository : HolidayRepository {
    override suspend fun getHolidays(
        year: Int,
        month: Int,
    ): Result<List<HolidayItem>> = Result.success(
        listOf(
            HolidayItem(
                dateKind = "01",
                dateName = "제헌절",
                isHoliday = "Y",
                locdate = 20260717,
                seq = 1,
            ),
        ),
    )
}

private fun jsonClient(responseBody: String): HttpClient = createHttpClient(
    platformClient = HttpClient(
        MockEngine {
            respond(
                content = responseBody,
                headers = JSON_HEADERS,
            )
        },
    ),
    enableNetworkLogging = false,
)

private val JSON_HEADERS = headersOf(
    HttpHeaders.ContentType,
    ContentType.Application.Json.toString(),
)

private const val VALID_EXCHANGE_RATE_RESPONSE = """
[
  {
    "result": 1,
    "cur_unit": "USD",
    "cur_nm": "미국 달러",
    "ttb": "1300.00",
    "tts": "1400.00",
    "deal_bas_r": "1350.00",
    "bkpr": "1350.00"
  }
]
"""
