package net.ifmain.hwanultoktok.kmp.presentation.state

import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert

data class AlertUiState(
    val isLoading: Boolean = false,
    val alerts: List<ExchangeRateAlert> = emptyList(),
    val errorMessage: String? = null,
    val isAddingAlert: Boolean = false
)