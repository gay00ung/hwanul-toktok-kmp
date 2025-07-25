package net.ifmain.hwanultoktok.kmp.domain.service

import io.ktor.util.date.getTimeMillis

interface NotificationService {
    suspend fun showNotification(
        title: String,
        message: String,
        notificationId: Int = getTimeMillis().toInt()
    )

    suspend fun requestNotificationPermission(): Boolean
    suspend fun isNotificationPermissionGranted(): Boolean

}