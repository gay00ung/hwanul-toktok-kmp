package net.ifmain.hwanultoktok.kmp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import net.ifmain.hwanultoktok.kmp.domain.model.AlertType
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetAlertsUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ManageAlertUseCase
import net.ifmain.hwanultoktok.kmp.domain.usecase.ScheduleExchangeRateCheckUseCase
import net.ifmain.hwanultoktok.kmp.presentation.state.AlertUiState
import net.ifmain.hwanultoktok.kmp.util.getCurrentDateTime

class AlertViewModel(
    private val getAlertsUseCase: GetAlertsUseCase,
    private val manageAlertUseCase: ManageAlertUseCase,
    private val scheduleExchangeRateCheckUseCase: ScheduleExchangeRateCheckUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertUiState())
    val uiState: StateFlow<AlertUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            getAlertsUseCase()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "알림을 불러오는데 실패했습니다"
                    )
                }
                .collect { alerts ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        alerts = alerts,
                        errorMessage = null
                    )
                }
        }
    }

    fun addAlert(currencyCode: String, alertType: AlertType, targetRate: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingAlert = true)
            
            try {
                val alert = ExchangeRateAlert(
                    currencyCode = currencyCode,
                    alertType = alertType,
                    targetRate = targetRate,
                    createdAt = getCurrentDateTime()
                )
                
                manageAlertUseCase.addAlert(alert)
                _uiState.value = _uiState.value.copy(isAddingAlert = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAddingAlert = false,
                    errorMessage = e.message ?: "알림 추가에 실패했습니다"
                )
            }
        }
    }

    fun deleteAlert(alertId: Long) {
        viewModelScope.launch {
            try {
                manageAlertUseCase.deleteAlert(alertId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "알림 삭제에 실패했습니다"
                )
            }
        }
    }

    fun updateAlert(alert: ExchangeRateAlert) {
        viewModelScope.launch {
            try {
                manageAlertUseCase.updateAlert(alert)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "알림 수정에 실패했습니다"
                )
            }
        }
    }

    fun startBackgroundMonitoring() {
        viewModelScope.launch {
            scheduleExchangeRateCheckUseCase(15)
        }
    }

    fun stopBackgroundMonitoring() {
        viewModelScope.launch {
            scheduleExchangeRateCheckUseCase.cancel()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}