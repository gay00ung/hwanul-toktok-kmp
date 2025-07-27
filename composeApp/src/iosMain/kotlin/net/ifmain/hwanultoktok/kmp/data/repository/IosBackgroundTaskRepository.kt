package net.ifmain.hwanultoktok.kmp.data.repository

import kotlinx.cinterop.*
import platform.BackgroundTasks.*
import platform.Foundation.*
import platform.UserNotifications.*
import net.ifmain.hwanultoktok.kmp.domain.repository.BackgroundTaskRepository
import net.ifmain.hwanultoktok.kmp.domain.usecase.CheckAlertConditionsUseCase
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IosBackgroundTaskRepository(
    private val checkAlertConditionsUseCase: CheckAlertConditionsUseCase,
    private val notificationService: NotificationService,
    private val alertRepository: AlertRepository
) : BackgroundTaskRepository {
    
    companion object {
        const val TASK_IDENTIFIER = "net.ifmain.hwanultoktok.kmp.exchangeRateCheck"
    }
    
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun scheduleExchangeRateCheck(hour: Int, minute: Int) {
        // Schedule local notification for precise timing
        scheduleDailyNotification(hour, minute)
        
        // Also schedule background task as backup
        val calendar = NSCalendar.currentCalendar
        val now = NSDate()
        
        val components = calendar.components(
            NSCalendarUnitYear or 
            NSCalendarUnitMonth or 
            NSCalendarUnitDay or 
            NSCalendarUnitHour or 
            NSCalendarUnitMinute,
            fromDate = now
        )
        
        components.hour = hour.toLong()
        components.minute = minute.toLong()
        components.second = 0
        
        val targetDate = calendar.dateFromComponents(components)
        
        // If target time is in the past, schedule for tomorrow
        val finalDate = if (targetDate!!.compare(now) == NSOrderedAscending) {
            calendar.dateByAddingUnit(
                NSCalendarUnitDay,
                value = 1,
                toDate = targetDate,
                options = 0u
            )
        } else {
            targetDate
        }

        val request = BGAppRefreshTaskRequest(TASK_IDENTIFIER)
        request.earliestBeginDate = finalDate

        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
    }
    
    private fun scheduleDailyNotification(hour: Int, minute: Int) {
        // Cancel existing scheduled notification
        UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(
            listOf("daily_exchange_rate_check")
        )
        
        // 매일 정해진 시간에 환율 체크를 위한 백그라운드 알림 스케줄
        // 이 알림은 백그라운드 태스크를 트리거하기 위한 용도
        val content = UNMutableNotificationContent().apply {
            setTitle("환율 체크 시작")
            setBody("환율 체크 중...")
            setSound(UNNotificationSound.defaultSound)
            // 사용자 정보에 타입 추가
            val userInfo = mutableMapOf<Any?, Any?>()
            userInfo["type"] = "exchange_rate_check"
            setUserInfo(userInfo)
        }
        
        val dateComponents = NSDateComponents().apply {
            this.hour = hour.toLong()
            this.minute = minute.toLong()
        }
        
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "daily_exchange_rate_check",
            content = content,
            trigger = trigger
        )
        
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("일일 알림 스케줄링 에러: ${error.localizedDescription}")
            } else {
                // 알림이 트리거될 때 환율 체크 실행
                executeExchangeRateCheckInBackground()
            }
        }
    }
    
    private fun executeExchangeRateCheckInBackground() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 환율 체크 로직 실행
                val alertResults = checkAlertConditionsUseCase()
                
                // 조건을 만족하는 알림 발송
                alertResults.filter { it.shouldTrigger }.forEach { result ->
                    notificationService.showNotification(
                        title = "환율 알림",
                        message = result.message,
                        notificationId = result.alert.id.hashCode()
                    )
                    
                    // 마지막 알림 시간 업데이트
                    alertRepository.updateLastTriggeredTime(
                        alertId = result.alert.id,
                        timestamp = NSDate().timeIntervalSince1970.toLong() * 1000
                    )
                }
            } catch (e: Exception) {
                println("환율 체크 실패: ${e.message}")
            }
        }
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