package net.ifmain.hwanultoktok.kmp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

internal const val REFRESH_COOLDOWN_MILLIS = 5_000L
internal const val EXCHANGE_RATE_LOAD_ERROR_MESSAGE =
    "환율 정보를 불러오지 못했습니다. 네트워크 연결을 확인한 뒤 다시 시도해주세요."
internal const val EXCHANGE_RATE_REFRESH_ERROR_MESSAGE =
    "최신 환율로 새로고침하지 못했습니다. 잠시 후 다시 시도해주세요."

class ExchangeRateViewModel(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val refreshExchangeRatesUseCase: RefreshExchangeRatesUseCase,
    private val getFavoriteUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getHolidaysUseCase: GetHolidaysUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExchangeRateUiState())
    val uiState: StateFlow<ExchangeRateUiState> = _uiState.asStateFlow()

    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        isInitialized = true
        loadExchangeRates()
        getFavoriteUseCase()
            .onEach { favorites ->
                _uiState.update { currentState ->
                    currentState.copy(
                        favoriteIds = favorites.map { it.toCurrencyCode }.toSet()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadExchangeRates() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return

        println("ExchangeRateViewModel: loadExchangeRates 호출")
        _uiState.update { currentState ->
            currentState.copy(isLoading = true, errorMessage = null)
        }

        viewModelScope.launch {
            try {
                var hasEmittedRates = false
                getExchangeRatesUseCase().collect { rates ->
                    hasEmittedRates = true

                    if (rates.isEmpty()) {
                        showLoadFailure()
                        return@collect
                    }

                    println("ExchangeRateViewModel: 데이터 수신 - ${rates.size}개 환율")
                    val updateTime = rates.first().timestamp

                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            exchangeRates = rates,
                            errorMessage = null,
                            lastUpdateTime = updateTime,
                        )
                    }

                    // 공휴일을 고려한 실제 데이터 기준일 계산
                    updateFormattedDataDate(updateTime)
                }

                if (!hasEmittedRates) {
                    showLoadFailure()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                showLoadFailure()
            }
        }
    }

    fun refreshExchangeRates() {
        if (!_uiState.value.canRefresh) return

        println("ExchangeRateViewModel: 새로고침 시작")
        _uiState.update { currentState ->
            currentState.copy(
                isRefreshing = true,
                isRefreshThrottled = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val result = refreshExchangeRatesUseCase()
                result.fold(
                    onSuccess = { rates ->
                        if (rates.isEmpty()) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    errorMessage = EXCHANGE_RATE_REFRESH_ERROR_MESSAGE,
                                )
                            }
                            return@fold
                        }

                        println("ExchangeRateViewModel: 새로고침 성공 - ${rates.size}개 환율")
                        val updateTime = rates.first().timestamp

                        _uiState.update { currentState ->
                            currentState.copy(
                                exchangeRates = rates,
                                errorMessage = null,
                                lastUpdateTime = updateTime,
                            )
                        }

                        // 공휴일을 고려한 실제 데이터 기준일 계산
                        updateFormattedDataDate(updateTime)
                    },
                    onFailure = {
                        _uiState.update { currentState ->
                            currentState.copy(
                                errorMessage = EXCHANGE_RATE_REFRESH_ERROR_MESSAGE,
                            )
                        }
                    },
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = EXCHANGE_RATE_REFRESH_ERROR_MESSAGE,
                    )
                }
            } finally {
                _uiState.update { currentState ->
                    currentState.copy(isRefreshing = false)
                }
            }

            delay(REFRESH_COOLDOWN_MILLIS)
            _uiState.update { currentState ->
                currentState.copy(isRefreshThrottled = false)
            }
        }
    }

    fun retryExchangeRates() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isRefreshing || currentState.isRefreshThrottled) {
            return
        }

        if (currentState.exchangeRates.isEmpty()) {
            loadExchangeRates()
        } else {
            refreshExchangeRates()
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

    fun toggleFavorite(
        currencyCode: String,
        onSuccess: suspend () -> Unit = {},
    ) {
        viewModelScope.launch {
            val result = toggleFavoriteUseCase(
                fromCurrencyCode = "KRW",
                toCurrencyCode = currencyCode
            )

            if (result.isSuccess) {
                onSuccess()
            }
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
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
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
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }

    private fun showLoadFailure() {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                errorMessage = EXCHANGE_RATE_LOAD_ERROR_MESSAGE,
            )
        }
    }
}
