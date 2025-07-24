package net.ifmain.hwanultoktok.kmp.service

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        if (!isNotificationPermissionGranted()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
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