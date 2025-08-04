package net.ifmain.hwanultoktok.kmp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.ifmain.hwanultoktok.kmp.data.mapper.toDomain
import net.ifmain.hwanultoktok.kmp.data.remote.KoreaExImBankApi
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository
import net.ifmain.hwanultoktok.kmp.util.getExchangeRateSearchDate

class ExchangeRateRepositoryImpl(
    private val api: KoreaExImBankApi,
    private val apiKey: String
) : ExchangeRateRepository {

    private var cachedRates: List<ExchangeRate> = emptyList()
    private var previousExchangeRates: List<ExchangeRate> = emptyList()

    override suspend fun getExchangeRates(): Flow<List<ExchangeRate>> = flow {
        if (cachedRates.isEmpty()) {
            val result = refreshExchangeRates()
            if (result.isSuccess) {
                emit(result.getOrNull() ?: emptyList())
            } else {
                emit(emptyList())
            }
        } else {
            emit(cachedRates)
        }
    }

    override suspend fun refreshExchangeRates(): Result<List<ExchangeRate>> {
        return try {
            val searchDate = getExchangeRateSearchDate()
            val response = api.getExchangeRates(apiKey, searchDate)
            val exchangeRates = response.map { it.toDomain() }
            cachedRates = exchangeRates
            Result.success(exchangeRates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPreviousExchangeRates(): List<ExchangeRate> {
        return previousExchangeRates
    }

    override suspend fun savePreviousExchangeRates(rates: List<ExchangeRate>) {
        previousExchangeRates = rates
    }
}