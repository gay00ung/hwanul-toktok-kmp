package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository

class RefreshExchangeRatesUseCase(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(): Result<List<ExchangeRate>> {
        println("RefreshExchangeRatesUseCase: API 호출 시작")
        val result = repository.refreshExchangeRates()
        if (result.isSuccess) {
            println("RefreshExchangeRatesUseCase: API 호출 성공 - ${result.getOrNull()?.size ?: 0}개 환율 데이터 받음")
        } else {
            println("RefreshExchangeRatesUseCase: API 호출 실패 - ${result.exceptionOrNull()?.message}")
        }
        return result
    }
}