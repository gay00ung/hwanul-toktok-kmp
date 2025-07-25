package net.ifmain.hwanultoktok.kmp.domain.repository

import kotlinx.coroutines.flow.Flow
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate

interface ExchangeRateRepository {
    suspend fun getExchangeRates(): Flow<List<ExchangeRate>>
    suspend fun refreshExchangeRates(): Result<List<ExchangeRate>>
    suspend fun getPreviousExchangeRates(): List<ExchangeRate>
    suspend fun savePreviousExchangeRates(rates: List<ExchangeRate>)
}