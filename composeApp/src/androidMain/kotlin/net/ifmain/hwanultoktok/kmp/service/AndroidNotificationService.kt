package net.ifmain.hwanultoktok.kmp.service

import android.Manifest
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import net.ifmain.hwanultoktok.kmp.R
import androidx.core.content.ContextCompat
import net.ifmain.hwanultoktok.kmp.domain.service.NotificationService

class AndroidNotificationService(
    private val context: Context
) : NotificationService {
    companion object {
        const val CHANNEL_ID = "exchange_rate_alerts"
        const val CHANNEL_NAME = "환율 알림"
        const val CHANNEL_DESCRIPTION = "환율 목표 도달 알림"
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
            setShowBadge(true)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    init {
        createNotificationChannel()
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun showNotification(
        title: String,
        message: String,
        notificationId: Int
    ) {
        println("AndroidNotificationService: showNotification 호출 - title: $title, message: $message")
        if (!isNotificationPermissionGranted()) {
            println("AndroidNotificationService: 알림 권한이 없습니다.")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            println("AndroidNotificationService: 알림 발송 성공")
        } catch (e: Exception) {
            println("AndroidNotificationService: 알림 발송 실패 - ${e.message}")
            e.printStackTrace()
        }
    }

    override suspend fun requestNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12 이하는 권한 필요 없음
        }

    }

    override suspend fun isNotificationPermissionGranted(): Boolean {
        return requestNotificationPermission()
    }
}