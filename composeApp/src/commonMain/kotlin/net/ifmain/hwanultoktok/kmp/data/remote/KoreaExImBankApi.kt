package net.ifmain.hwanultoktok.kmp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import net.ifmain.hwanultoktok.kmp.data.remote.dto.ExchangeRateDto

class KoreaExImBankApi(private val httpClient: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://oapi.koreaexim.go.kr"
        private const val EXCHANGE_RATE_ENDPOINT = "/site/program/financial/exchangeJSON"
    }
    
    suspend fun getExchangeRates(authKey: String, searchDate: String? = null): List<ExchangeRateDto> {
        return httpClient.get("$BASE_URL$EXCHANGE_RATE_ENDPOINT") {
            parameter("authkey", authKey)
            parameter("data", "AP01")
            searchDate?.let { parameter("searchdate", it) }
        }.body()
    }
}