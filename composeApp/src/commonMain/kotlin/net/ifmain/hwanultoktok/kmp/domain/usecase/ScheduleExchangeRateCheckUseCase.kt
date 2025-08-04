package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository

class ScheduleExchangeRateCheckUseCase(
    private val backgroundTaskRepository: BackgroundTaskRepository
) {
    suspend operator fun invoke(hour: Int = 11, minute: Int = 30) {
        backgroundTaskRepository.scheduleExchangeRateCheck(hour, minute)
    }

    suspend fun cancel() {
        backgroundTaskRepository.cancelExchangeRateCheck()
    }
}