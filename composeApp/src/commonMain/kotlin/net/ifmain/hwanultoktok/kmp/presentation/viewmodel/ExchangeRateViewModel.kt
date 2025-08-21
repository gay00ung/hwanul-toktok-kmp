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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetFavoritesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetHolidaysUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.RefreshExchangeRatesUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ToggleFavoriteUseCase
import net.ifmain.hwanultoktok.kmp.presentation.state.ExchangeRateUiState
import net.ifmain.hwanultoktok.kmp.util.formatDateTime
import net.ifmain.hwanultoktok.kmp.util.getDataBaseDateWithoutHoliday

class ExchangeRateViewModel(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val refreshExchangeRatesUseCase: RefreshExchangeRatesUseCase,
    getFavoriteUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getHolidaysUseCase: GetHolidaysUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExchangeRateUiState())
    val uiState: StateFlow<ExchangeRateUiState> = _uiState.asStateFlow()

    init {
        println("ExchangeRateViewModel: 초기화 시작")
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
        println("ExchangeRateViewModel: loadExchangeRates 호출")
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
                        println("ExchangeRateViewModel: 데이터 수신 - ${rates.size}개 환율")
                        val updateTime = rates.firstOrNull()?.timestamp

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            exchangeRates = rates,
                            errorMessage = null,
                            lastUpdateTime = updateTime
                        )

                        // 공휴일을 고려한 실제 데이터 기준일 계산
                        updateTime?.let { updateFormattedDataDate(it) }
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
        println("ExchangeRateViewModel: 새로고침 시작")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            val result = refreshExchangeRatesUseCase()
            result.fold(
                onSuccess = { rates ->
                    println("ExchangeRateViewModel: 새로고침 성공 - ${rates.size}개 환율")
                    val updateTime = rates.firstOrNull()?.timestamp

                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        exchangeRates = rates,
                        errorMessage = null,
                        lastUpdateTime = updateTime
                    )

                    // 공휴일을 고려한 실제 데이터 기준일 계산
                    updateTime?.let { updateFormattedDataDate(it) }
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

    private fun updateFormattedDataDate(updateTime: LocalDateTime) {
        viewModelScope.launch {
            try {
                val formattedDate = formatDateTime(updateTime, getHolidaysUseCase)
                _uiState.update { currentState ->
                    currentState.copy(formattedDataDate = formattedDate)
                }
            } catch (e: Exception) {
                // 공휴일 데이터 로드 실패시 기본 포맷 사용 (공휴일 체크 없이)
                val dataDate = getDataBaseDateWithoutHoliday(updateTime)
                val basicFormat =
                    "${dataDate.year}년 ${dataDate.month.number}월 ${dataDate.day}일 고시환율"
                _uiState.update { currentState ->
                    currentState.copy(formattedDataDate = basicFormat)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}