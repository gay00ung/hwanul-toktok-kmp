package net.ifmain.hwanultoktok.kmp.domain.usecase

import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateChange
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository

class MonitorExchangeRateUseCase(
    private val exchangeRateRepository: ExchangeRateRepository
) {
    suspend operator fun invoke(): Flow<List<ExchangeRateChange>> = flow {
        val currentRates = exchangeRateRepository.getExchangeRates().first()
        val previousRates = exchangeRateRepository.getPreviousExchangeRates()

        val changes = currentRates.mapNotNull { current ->
            val previous = previousRates.find { it.currencyCode == current.currencyCode }
            if (previous != null) {
                val changeAmount = current.baseRate - previous.baseRate
                val changePercent = (changeAmount / previous.baseRate) * 100

                ExchangeRateChange(
                    currencyCode = current.currencyCode,
                    previousRate = previous.baseRate,
                    currentRate = current.baseRate,
                    changeAmount = changeAmount,
                    changePercentage = changePercent,
                    timestamp = getTimeMillis()
                )
            } else null
        }

        // 현재 환율을 이전 환율로 저장
        exchangeRateRepository.savePreviousExchangeRates(currentRates)

        emit(changes)
    }
}


