package net.ifmain.hwanultoktok.kmp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.ifmain.hwanultoktok.kmp.domain.model.HolidayItem
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetHolidaysUseCase

class HolidayViewModel(
    private val getHolidaysUseCase: GetHolidaysUseCase
) : ViewModel() {

    private val _holidays = MutableStateFlow<List<HolidayItem>>(emptyList())
    val holidays: StateFlow<List<HolidayItem>> = _holidays.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadHolidays(year: Int, month: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getHolidaysUseCase(year, month)
                .onSuccess { holidayList ->
                    _holidays.value = holidayList
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
}