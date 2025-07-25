package net.ifmain.hwanultoktok.kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository

class GetExchangeRatesUseCase(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(): Flow<List<ExchangeRate>> {
        return repository.getExchangeRates()
    }
}