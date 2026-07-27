package net.ifmain.hwanultoktok.kmp.presentation.viewmodel

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.domain.model.FavoriteCurrencyPair
import net.ifmain.hwanultoktok.kmp.domain.model.HolidayItem
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.FavoriteRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.HolidayRepository
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetFavoritesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetHolidaysUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.RefreshExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ToggleFavoriteUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeRateViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialize_called_multiple_times_loads_exchange_rates_once() = runTest(mainDispatcher) {
        val exchangeRateRepository = FakeExchangeRateRepository()
        val favoriteRepository = FakeFavoriteRepository()
        val holidayRepository = FakeHolidayRepository()
        val viewModel = ExchangeRateViewModel(
            getExchangeRatesUseCase = GetExchangeRatesUseCase(exchangeRateRepository),
            refreshExchangeRatesUseCase = RefreshExchangeRatesUseCase(exchangeRateRepository),
            getFavoriteUseCase = GetFavoritesUseCase(favoriteRepository),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository),
            getHolidaysUseCase = GetHolidaysUseCase(holidayRepository),
        )

        viewModel.initialize()
        viewModel.initialize()
        advanceUntilIdle()

        assertEquals(1, exchangeRateRepository.getExchangeRatesCallCount)
        assertEquals(1, favoriteRepository.getAllFavoritesCallCount)
    }

    @Test
    fun initial_load_failure_stops_loading_and_shows_retryable_message() =
        runTest(mainDispatcher) {
            val exchangeRateRepository = FakeExchangeRateRepository(
                loadFlowOverride = {
                    flow {
                        throw IllegalStateException("network unavailable")
                    }
                },
            )
            val viewModel = createViewModel(exchangeRateRepository)

            viewModel.loadExchangeRates()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.exchangeRates.isEmpty())
            assertEquals(
                EXCHANGE_RATE_LOAD_ERROR_MESSAGE,
                viewModel.uiState.value.errorMessage,
            )
            assertTrue(viewModel.uiState.value.canRefresh)
        }

    @Test
    fun load_blocks_duplicate_requests_while_initial_request_is_running() =
        runTest(mainDispatcher) {
            val loadGate = CompletableDeferred<Unit>()
            val exchangeRateRepository = FakeExchangeRateRepository(
                loadFlowOverride = {
                    flow {
                        loadGate.await()
                        emit(sampleExchangeRates())
                    }
                },
            )
            val viewModel = createViewModel(exchangeRateRepository)

            viewModel.loadExchangeRates()
            viewModel.loadExchangeRates()
            runCurrent()

            assertEquals(1, exchangeRateRepository.getExchangeRatesCallCount)

            loadGate.complete(Unit)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun retry_after_initial_load_failure_requests_rates_again_and_recovers() =
        runTest(mainDispatcher) {
            var loadAttempt = 0
            val exchangeRateRepository = FakeExchangeRateRepository(
                loadFlowOverride = {
                    loadAttempt += 1
                    if (loadAttempt == 1) {
                        flow {
                            throw IllegalStateException("network unavailable")
                        }
                    } else {
                        flowOf(sampleExchangeRates())
                    }
                },
            )
            val viewModel = createViewModel(exchangeRateRepository)

            viewModel.loadExchangeRates()
            advanceUntilIdle()
            viewModel.retryExchangeRates()
            advanceUntilIdle()

            assertEquals(2, exchangeRateRepository.getExchangeRatesCallCount)
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(sampleExchangeRates(), viewModel.uiState.value.exchangeRates)
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun toggle_favorite_invokes_success_callback_after_repository_update() = runTest(mainDispatcher) {
        val callOrder = mutableListOf<String>()
        val favoriteRepository = FakeFavoriteRepository(
            onAddFavorite = { callOrder += "stored" },
        )
        val viewModel = ExchangeRateViewModel(
            getExchangeRatesUseCase = GetExchangeRatesUseCase(FakeExchangeRateRepository()),
            refreshExchangeRatesUseCase = RefreshExchangeRatesUseCase(FakeExchangeRateRepository()),
            getFavoriteUseCase = GetFavoritesUseCase(favoriteRepository),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository),
            getHolidaysUseCase = GetHolidaysUseCase(FakeHolidayRepository()),
        )

        viewModel.toggleFavorite("USD") {
            callOrder += "widget"
        }
        advanceUntilIdle()

        assertEquals(listOf("stored", "widget"), callOrder)
    }

    @Test
    fun toggle_favorite_does_not_invoke_success_callback_when_repository_update_fails() =
        runTest(mainDispatcher) {
            var callbackCalled = false
            val favoriteRepository = FakeFavoriteRepository(
                addFavoriteResult = Result.failure(IllegalStateException("write failed")),
            )
            val viewModel = ExchangeRateViewModel(
                getExchangeRatesUseCase = GetExchangeRatesUseCase(FakeExchangeRateRepository()),
                refreshExchangeRatesUseCase = RefreshExchangeRatesUseCase(FakeExchangeRateRepository()),
                getFavoriteUseCase = GetFavoritesUseCase(favoriteRepository),
                toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository),
                getHolidaysUseCase = GetHolidaysUseCase(FakeHolidayRepository()),
            )

            viewModel.toggleFavorite("USD") {
                callbackCalled = true
            }
            advanceUntilIdle()

            assertFalse(callbackCalled)
        }

    @Test
    fun refresh_blocks_duplicate_requests_and_keeps_five_second_cooldown_after_completion() =
        runTest(mainDispatcher) {
            val refreshResult = CompletableDeferred<Result<List<ExchangeRate>>>()
            val exchangeRateRepository = FakeExchangeRateRepository(
                refreshResultOverride = { refreshResult.await() },
            )
            val viewModel = ExchangeRateViewModel(
                getExchangeRatesUseCase = GetExchangeRatesUseCase(exchangeRateRepository),
                refreshExchangeRatesUseCase = RefreshExchangeRatesUseCase(exchangeRateRepository),
                getFavoriteUseCase = GetFavoritesUseCase(FakeFavoriteRepository()),
                toggleFavoriteUseCase = ToggleFavoriteUseCase(FakeFavoriteRepository()),
                getHolidaysUseCase = GetHolidaysUseCase(FakeHolidayRepository()),
            )

            viewModel.refreshExchangeRates()
            viewModel.refreshExchangeRates()

            assertTrue(viewModel.uiState.value.isRefreshing)
            assertFalse(viewModel.uiState.value.canRefresh)

            runCurrent()
            assertEquals(1, exchangeRateRepository.refreshExchangeRatesCallCount)

            refreshResult.complete(Result.success(emptyList()))
            runCurrent()

            assertFalse(viewModel.uiState.value.isRefreshing)
            assertTrue(viewModel.uiState.value.isRefreshThrottled)
            assertFalse(viewModel.uiState.value.canRefresh)

            viewModel.refreshExchangeRates()
            runCurrent()
            assertEquals(1, exchangeRateRepository.refreshExchangeRatesCallCount)

            advanceTimeBy(REFRESH_COOLDOWN_MILLIS - 1)
            runCurrent()
            assertFalse(viewModel.uiState.value.canRefresh)

            advanceTimeBy(1)
            runCurrent()
            assertTrue(viewModel.uiState.value.canRefresh)

            viewModel.refreshExchangeRates()
            advanceUntilIdle()

            assertEquals(2, exchangeRateRepository.refreshExchangeRatesCallCount)
        }

    private fun createViewModel(
        exchangeRateRepository: ExchangeRateRepository,
    ): ExchangeRateViewModel {
        val favoriteRepository = FakeFavoriteRepository()
        return ExchangeRateViewModel(
            getExchangeRatesUseCase = GetExchangeRatesUseCase(exchangeRateRepository),
            refreshExchangeRatesUseCase = RefreshExchangeRatesUseCase(exchangeRateRepository),
            getFavoriteUseCase = GetFavoritesUseCase(favoriteRepository),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository),
            getHolidaysUseCase = GetHolidaysUseCase(FakeHolidayRepository()),
        )
    }
}

private class FakeExchangeRateRepository(
    private val refreshResultOverride: (suspend () -> Result<List<ExchangeRate>>)? = null,
    private val loadFlowOverride: (suspend () -> Flow<List<ExchangeRate>>)? = null,
) : ExchangeRateRepository {
    var getExchangeRatesCallCount = 0
        private set
    var refreshExchangeRatesCallCount = 0
        private set

    private val rates = sampleExchangeRates()

    override suspend fun getExchangeRates(): Flow<List<ExchangeRate>> {
        getExchangeRatesCallCount += 1
        return loadFlowOverride?.invoke() ?: flowOf(rates)
    }

    override suspend fun refreshExchangeRates(): Result<List<ExchangeRate>> {
        refreshExchangeRatesCallCount += 1
        return refreshResultOverride?.invoke() ?: Result.success(rates)
    }

    override suspend fun getPreviousExchangeRates(): List<ExchangeRate> = emptyList()

    override suspend fun savePreviousExchangeRates(rates: List<ExchangeRate>) = Unit
}

private class FakeFavoriteRepository(
    private val addFavoriteResult: Result<Unit> = Result.success(Unit),
    private val onAddFavorite: () -> Unit = {},
) : FavoriteRepository {
    var getAllFavoritesCallCount = 0
        private set

    override fun getAllFavorites(): Flow<List<FavoriteCurrencyPair>> {
        getAllFavoritesCallCount += 1
        return flowOf(emptyList())
    }

    override suspend fun addFavorite(
        fromCurrencyCode: String,
        toCurrencyCode: String,
    ): Result<Unit> {
        onAddFavorite()
        return addFavoriteResult
    }

    override suspend fun removeFavorite(
        fromCurrencyCode: String,
        toCurrencyCode: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun isFavorite(fromCurrencyCode: String, toCurrencyCode: String): Boolean = false

    override suspend fun updateDisplayOrder(favoriteId: Long, newOrder: Int): Result<Unit> =
        Result.success(Unit)
}

private class FakeHolidayRepository : HolidayRepository {
    override suspend fun getHolidays(year: Int, month: Int): Result<List<HolidayItem>> =
        Result.success(emptyList())
}

private fun sampleExchangeRates(): List<ExchangeRate> = listOf(
    ExchangeRate(
        currencyCode = "USD",
        currencyName = "미국 달러",
        currencyUnit = "USD",
        buyingRate = 1_300.0,
        sellingRate = 1_400.0,
        baseRate = 1_350.0,
        bookPrice = 1_350.0,
        timestamp = LocalDateTime(2026, 7, 20, 12, 0),
    ),
)
