package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository

class RefreshExchangeRatesUseCase(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(): Result<List<ExchangeRate>> {
        return repository.refreshExchangeRates()
    }
}