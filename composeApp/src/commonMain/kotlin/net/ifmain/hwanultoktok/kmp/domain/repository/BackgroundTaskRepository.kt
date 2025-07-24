package net.ifmain.hwanultoktok.kmp.domain.repository

interface BackgroundTaskRepository {
    /**
     * 추후 시간 대 별 환율 정보를 받아오는 api 사용 시 활용 예정
     * suspend fun scheduleExchangeRateCheck(intervalMinutes: Long)
     */
    suspend fun scheduleExchangeRateCheck(hour: Int, minute: Int)
    suspend fun cancelExchangeRateCheck()
    suspend fun isExchangeRateCheckScheduled(): Boolean
    suspend fun executeExchangeRateCheck()
}
