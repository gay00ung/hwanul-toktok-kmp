package net.ifmain.hwanultoktok.kmp.data.repository

import kotlinx.cinterop.*
import platform.BackgroundTasks.*
import platform.Foundation.*
import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository

class IosBackgroundTaskRepository : BackgroundTaskRepository {
    
    companion object {
        const val TASK_IDENTIFIER = "net.ifmain.hwanultoktok.kmp.exchangeRateCheck"
    }
    
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun scheduleExchangeRateCheck(hour: Int, minute: Int) {
        val twentyFourHours = 24.0 * 60.0 * 60.0

        val request = BGAppRefreshTaskRequest(TASK_IDENTIFIER)
        request.earliestBeginDate = NSDate().dateByAddingTimeInterval(twentyFourHours)

        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
    }


    override suspend fun cancelExchangeRateCheck() {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(TASK_IDENTIFIER)
    }
    
    override suspend fun isExchangeRateCheckScheduled(): Boolean {
        var isScheduled = false
        BGTaskScheduler.sharedScheduler.getPendingTaskRequestsWithCompletionHandler { tasks ->
            isScheduled = tasks?.any { (it as? BGTaskRequest)?.identifier == TASK_IDENTIFIER } ?: false
        }
        return isScheduled
    }
    
    override suspend fun executeExchangeRateCheck() {
        println("iOS doesn't support immediate background task execution")
    }
}