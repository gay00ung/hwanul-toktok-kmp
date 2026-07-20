package net.ifmain.hwanultoktok.kmp.worker

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.ifmain.hwanultoktok.kmp.domain.model.AlertType
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
import net.ifmain.hwanultoktok.kmp.domain.repository.AlertRepository
import net.ifmain.hwanultoktok.kmp.util.getCurrentDateTime
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class ExchangeRateWorkExecutorDeviceTest {

    @Test
    fun refreshesPostsOfficialUnitAndDoesNotPostAgainWhileDisarmed() = runTest(
        timeout = 90.seconds,
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        assertEquals(
            PackageManager.PERMISSION_GRANTED,
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS),
        )
        val koin = GlobalContext.get()
        val alertRepository = koin.get<AlertRepository>()
        val executor = koin.get<ExchangeRateWorkExecutor>()
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val existingAlerts = alertRepository.getAllAlerts().first()

        existingAlerts.forEach { alert ->
            alertRepository.updateAlert(alert.copy(isEnabled = false))
        }

        val testTargetRate = 0.1234567
        alertRepository.insertAlert(
            ExchangeRateAlert(
                currencyCode = "JPY",
                alertType = AlertType.ABOVE,
                targetRate = testTargetRate,
                createdAt = getCurrentDateTime(),
            ),
        )
        val testAlert = alertRepository.getAllAlerts().first().single { alert ->
            alert.currencyCode == "JPY" && alert.targetRate == testTargetRate
        }
        val notificationId = testAlert.id.hashCode()

        try {
            executor.execute()

            val firstNotification = notificationManager.activeNotifications
                .firstOrNull { it.id == notificationId }
            assertNotNull(firstNotification)
            val firstMessage = firstNotification.notification.extras
                .getCharSequence(Notification.EXTRA_TEXT)
                ?.toString()
            assertNotNull(firstMessage)
            assertTrue(firstMessage.startsWith("JPY(100) 환율이"))

            val persistedAlert = alertRepository.getAllAlerts().first()
                .single { it.id == testAlert.id }
            assertFalse(persistedAlert.isArmed)

            executor.execute()

            val secondNotification = notificationManager.activeNotifications
                .firstOrNull { it.id == notificationId }
            assertNotNull(secondNotification)
            assertEquals(firstNotification.postTime, secondNotification.postTime)
        } finally {
            notificationManager.cancel(notificationId)
            alertRepository.deleteAlert(testAlert.id)
            existingAlerts.forEach { alertRepository.updateAlert(it) }
        }
    }
}
