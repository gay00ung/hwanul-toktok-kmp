package net.ifmain.hwanultoktok.kmp.presentation.state

import kotlinx.datetime.LocalDateTime
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate

data class ExchangeRateUiState(
    val isLoading: Boolean = false,
    val exchangeRates: List<ExchangeRate> = emptyList(),
    val selectedCurrencies: Set<String> = setOf(
        "USD", "EUR", "JPY", "CNH", "GBP", "AUD", "CAD", "CHF",
        "HKD", "SEK", "NZD", "SGD", "NOK", "MXN", "INR", "RUB",
        "ZAR", "TRY", "BRL", "TWD", "DKK", "PLN", "THB", "MYR"
    ),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    val favoriteIds: Set<String> = emptySet(),
    val showFavoritesOnly: Boolean = false,
    val lastUpdateTime: LocalDateTime? = null,
) {
    val filteredExchangeRates: List<ExchangeRate>
        get() = exchangeRates.filter { rate ->
            val isInSelectedCurrencies = selectedCurrencies.contains(rate.currencyCode)
            val isFavorite = favoriteIds.contains(rate.currencyCode)

            when {
                showFavoritesOnly -> isInSelectedCurrencies && isFavorite
                else -> isInSelectedCurrencies
            }
        }
}