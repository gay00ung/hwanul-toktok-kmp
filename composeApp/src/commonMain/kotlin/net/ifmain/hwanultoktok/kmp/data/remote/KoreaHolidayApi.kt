package net.ifmain.hwanultoktok.kmp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import net.ifmain.hwanultoktok.kmp.domain.model.HolidayResponse

class KoreaHolidayApi(private val httpClient: HttpClient) {
    companion object {
        private const val BASE_URL = "https://apis.data.go.kr"
        private const val HOLIDAY_ENDPOINT = "/B090041/openapi/service/SpcdeInfoService"
    }

    suspend fun getHolidays(serviceKey: String, year: Int, month: Int): HolidayResponse {
        return httpClient.get("$BASE_URL$HOLIDAY_ENDPOINT/getRestDeInfo") {
            parameter("ServiceKey", serviceKey)
            parameter("solYear", year)
            parameter("solMonth", month.toString().padStart(2, '0'))
            parameter("_type", "json")
            parameter("numOfRows", 10)
        }.body()
    }
}