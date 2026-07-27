package net.ifmain.hwanultoktok.kmp.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import net.ifmain.hwanultoktok.kmp.data.mapper.toDomain
import net.ifmain.hwanultoktok.kmp.data.remote.KoreaExImBankApi
import net.ifmain.hwanultoktok.kmp.data.remote.dto.ExchangeRateDto
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.domain.repository.ExchangeRateRepository
import net.ifmain.hwanultoktok.kmp.domain.repository.HolidayRepository
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetHolidaysUseCase
import net.ifmain.hwanultoktok.kmp.util.getDataBaseDateWithFallback
import net.ifmain.hwanultoktok.kmp.util.getDataBaseDateWithoutHoliday
import net.ifmain.hwanultoktok.kmp.util.getCurrentDateTime

class ExchangeRateRepositoryImpl(
    private val api: KoreaExImBankApi,
    private val apiKey: String,
    private val holidayRepository: HolidayRepository,
    private val nowProvider: () -> LocalDateTime = ::getCurrentDateTime,
) : ExchangeRateRepository {

    private var cachedRates: List<ExchangeRate> = emptyList()
    private var previousExchangeRates: List<ExchangeRate> = emptyList()

    override suspend fun getExchangeRates(): Flow<List<ExchangeRate>> = flow {
        println("ExchangeRateRepositoryImpl: getExchangeRates 호출")
        if (cachedRates.isEmpty()) {
            println("ExchangeRateRepositoryImpl: 캐시가 비어있음, API 호출 시작")
            val rates = refreshExchangeRates().getOrThrow()
            println("ExchangeRateRepositoryImpl: 데이터 방출 - ${rates.size}개")
            emit(rates)
        } else {
            println("ExchangeRateRepositoryImpl: 캐시된 데이터 방출 - ${cachedRates.size}개")
            emit(cachedRates)
        }
    }

    override suspend fun refreshExchangeRates(): Result<List<ExchangeRate>> {
        return try {
            val exchangeRates = coroutineScope {
                val now = nowProvider()
                val fallbackDate = getDataBaseDateWithoutHoliday(now)
                val holidayAwareDate = async {
                    getDataBaseDateWithFallback(now, createGetHolidaysUseCase())
                }

                var dataDate = fallbackDate
                var response = getExchangeRateResponse(dataDate)

                if (response.isEmpty()) {
                    dataDate = holidayAwareDate.await()
                    if (dataDate != fallbackDate) {
                        response = getExchangeRateResponse(dataDate)
                    }
                } else {
                    holidayAwareDate.cancel()
                }

                check(response.isNotEmpty()) {
                    "조회된 환율 정보가 없습니다."
                }
                println(
                    "ExchangeRateRepositoryImpl: API 응답 받음 - " +
                        "${response.size}개 데이터 (기준일: $dataDate)",
                )
                response.map { it.toDomain() }
            }

            cachedRates = exchangeRates
            println("ExchangeRateRepositoryImpl: 데이터 변환 및 캐시 저장 완료")
            Result.success(exchangeRates)
        } catch (e: CancellationException) {
            throw e
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
    
    // HolidayRepository를 GetHolidaysUseCase로 변환하는 헬퍼 함수
    private fun createGetHolidaysUseCase(): GetHolidaysUseCase {
        return GetHolidaysUseCase(holidayRepository)
    }

    private suspend fun getExchangeRateResponse(
        dataDate: LocalDate,
    ): List<ExchangeRateDto> {
        val searchDate = dataDate.toString().replace("-", "")
        println("ExchangeRateRepositoryImpl: API 요청 시작 - 날짜: $searchDate")
        return api.getExchangeRates(
            authKey = apiKey,
            searchDate = searchDate,
        )
    }
}
