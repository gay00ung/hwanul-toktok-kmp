package net.ifmain.hwanultoktok.kmp.alarm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ExchangeRateAlarmSchedulerDeviceTest {

    @Test
    fun registersCancelsAndRestoresTheNextAlarm() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scheduler = ExchangeRateAlarmScheduler(context)
        val now = Instant.now()

        scheduler.cancel()

        try {
            val registration = scheduler.scheduleNext(now = now)

            assertTrue(registration.triggerAtMillis > now.toEpochMilli())
            assertEquals(
                registration.triggerAtMillis,
                scheduler.scheduledTriggerAtMillis(),
            )
            assertEquals(scheduler.canScheduleExactAlarms(), registration.isExact)
            assertTrue(scheduler.isScheduled(now))

            scheduler.cancel()

            assertFalse(scheduler.isScheduled(now))
        } finally {
            scheduler.scheduleNext()
        }
    }
}
