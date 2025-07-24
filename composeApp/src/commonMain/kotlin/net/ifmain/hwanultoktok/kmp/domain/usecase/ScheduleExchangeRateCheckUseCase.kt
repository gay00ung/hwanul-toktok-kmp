package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository

class ScheduleExchangeRateCheckUseCase(
    private val backgroundTaskRepository: BackgroundTaskRepository
) {
    suspend operator fun invoke(intervalMinutes: Long = 15) {
        backgroundTaskRepository.scheduleExchangeRateCheck(11, 30)
    }

    suspend fun cancel() {
        backgroundTaskRepository.cancelExchangeRateCheck()
    }

    suspend fun isScheduled(): Boolean {
        return backgroundTaskRepository.isExchangeRateCheckScheduled()
    }
}