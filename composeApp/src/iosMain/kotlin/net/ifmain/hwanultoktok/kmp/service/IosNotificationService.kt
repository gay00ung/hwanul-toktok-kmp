package net.ifmain.hwanultoktok.kmp.service

import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import platform.Foundation.*
import platform.UserNotifications.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IosNotificationService : NotificationService {
    
    companion object {
        const val DAILY_NOTIFICATION_ID = "daily_exchange_rate_check"
    }
    
    suspend fun scheduleDailyNotification(hour: Int, minute: Int) {
        if (!isNotificationPermissionGranted()) {
            requestNotificationPermission()
            return
        }
        
        // Cancel existing scheduled notification
        UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(
            listOf(DAILY_NOTIFICATION_ID)
        )
        
        val content = UNMutableNotificationContent().apply {
            setTitle("환율 확인")
            setBody("오늘의 환율을 확인해보세요!")
            setSound(UNNotificationSound.defaultSound)
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
            identifier = DAILY_NOTIFICATION_ID,
            content = content,
            trigger = trigger
        )
        
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("일일 알림 스케줄링 에러: ${error.localizedDescription}")
            }
        }
    }
    override suspend fun showNotification(
        title: String,
        message: String,
        notificationId: Int
    ) {
        if (!isNotificationPermissionGranted()) {
            requestNotificationPermission()
            return
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
            setSound(UNNotificationSound.defaultSound)
        }

        // 즉시 알림을 위해 0.1초로 설정
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, false)
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationId.toString(),
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("iOS 알림 발송 에러: ${error.localizedDescription}")
            }
        }
    }

    override suspend fun requestNotificationPermission(): Boolean = suspendCoroutine { continuation ->
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ ->
            continuation.resume(granted)

        }

    }

    override suspend fun isNotificationPermissionGranted(): Boolean = suspendCoroutine { continuation ->
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            continuation.resume(settings?.authorizationStatus == UNAuthorizationStatusAuthorized)
        }
    }

}