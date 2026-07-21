package net.ifmain.hwanultoktok.kmp.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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
}

private class FakeExchangeRateRepository : ExchangeRateRepository {
    var getExchangeRatesCallCount = 0
        private set

    private val rates = listOf(
        ExchangeRate(
            currencyCode = "USD",
            currencyName = "미국 달러",
            currencyUnit = "USD",
            buyingRate = 1_300.0,
            sellingRate = 1_400.0,
            baseRate = 1_350.0,
            bookPrice = 1_350.0,
            timestamp = LocalDateTime(2026, 7, 20, 12, 0),
        )
    )

    override suspend fun getExchangeRates(): Flow<List<ExchangeRate>> {
        getExchangeRatesCallCount += 1
        return flowOf(rates)
    }

    override suspend fun refreshExchangeRates(): Result<List<ExchangeRate>> = Result.success(rates)

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
