package net.ifmain.hwanultoktok.kmp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetFavoritesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.RefreshExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ToggleFavoriteUseCase
import net.ifmain.hwanultoktok.kmp.presentation.state.ExchangeRateUiState

class ExchangeRateViewModel(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val refreshExchangeRatesUseCase: RefreshExchangeRatesUseCase,
    private val getFavoriteUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExchangeRateUiState())
    val uiState: StateFlow<ExchangeRateUiState> = _uiState.asStateFlow()

    init {
        loadExchangeRates()
        getFavoriteUseCase()
            .onEach { favorites ->
                _uiState.update { currentState ->
                    currentState.copy(
                        favoriteIds = favorites.map { it.toCurrencyCode }.toSet()
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun loadExchangeRates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                getExchangeRatesUseCase()
                    .catch { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "알 수 없는 오류가 발생했습니다"
                        )
                    }
                    .collect { rates ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            exchangeRates = rates,
                            errorMessage = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다"
                )
            }
        }
    }

    fun refreshExchangeRates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            val result = refreshExchangeRatesUseCase()
            result.fold(
                onSuccess = { rates ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        exchangeRates = rates,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = error.message ?: "새로고침에 실패했습니다"
                    )
                }
            )
        }
    }

    fun toggleCurrencySelection(currencyCode: String) {
        val currentSelected = _uiState.value.selectedCurrencies.toMutableSet()
        if (currentSelected.contains(currencyCode)) {
            currentSelected.remove(currencyCode)
        } else {
            currentSelected.add(currencyCode)
        }
        _uiState.value = _uiState.value.copy(selectedCurrencies = currentSelected)
    }
    fun toggleFavorite(currencyCode: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(
                fromCurrencyCode = "KRW",
                toCurrencyCode = currencyCode
            )
        }
    }
    fun toggleFavoritesFilter() {
        _uiState.update { currentState ->
            currentState.copy(showFavoritesOnly = !currentState.showFavoritesOnly)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}