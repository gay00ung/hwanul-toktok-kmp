package net.ifmain.hwanultoktok.kmp.service

import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IosNotificationService : NotificationService {
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

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, false)
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationId.toString(),
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request, null)
    }

    override suspend fun requestNotificationPermission(): Boolean = suspendCoroutine { contiunation ->
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ ->
            contiunation.resume(granted)

        }

    }

    override suspend fun isNotificationPermissionGranted(): Boolean = suspendCoroutine { continuation ->
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            continuation.resume(settings?.authorizationStatus == UNAuthorizationStatusAuthorized)
        }
    }

}