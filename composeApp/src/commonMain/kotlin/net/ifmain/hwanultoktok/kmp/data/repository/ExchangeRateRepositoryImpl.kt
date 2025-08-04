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
        println("ExchangeRateRepositoryImpl: getExchangeRates 호출")
        if (cachedRates.isEmpty()) {
            println("ExchangeRateRepositoryImpl: 캐시가 비어있음, API 호출 시작")
            val result = refreshExchangeRates()
            if (result.isSuccess) {
                println("ExchangeRateRepositoryImpl: 데이터 방출 - ${result.getOrNull()?.size ?: 0}개")
                emit(result.getOrNull() ?: emptyList())
            } else {
                println("ExchangeRateRepositoryImpl: API 실패로 빈 리스트 방출")
                emit(emptyList())
            }
        } else {
            println("ExchangeRateRepositoryImpl: 캐시된 데이터 방출 - ${cachedRates.size}개")
            emit(cachedRates)
        }
    }

    override suspend fun refreshExchangeRates(): Result<List<ExchangeRate>> {
        return try {
            val searchDate = getExchangeRateSearchDate()
            println("ExchangeRateRepositoryImpl: API 요청 시작 - 날짜: $searchDate")
            val response = api.getExchangeRates(apiKey, searchDate)
            println("ExchangeRateRepositoryImpl: API 응답 받음 - ${response.size}개 데이터")
            val exchangeRates = response.map { it.toDomain() }
            cachedRates = exchangeRates
            println("ExchangeRateRepositoryImpl: 데이터 변환 및 캐시 저장 완료")
            Result.success(exchangeRates)
        } catch (e: Exception) {
            println("ExchangeRateRepositoryImpl: API 호출 오류 - ${e.message}")
            e.printStackTrace()
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