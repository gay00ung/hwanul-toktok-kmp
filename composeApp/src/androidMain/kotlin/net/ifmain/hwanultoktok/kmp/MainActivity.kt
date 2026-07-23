package net.ifmain.hwanultoktok.kmp

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ifmain.hwanultoktok.kmp.alarm.ExchangeRateAlarmScheduler
import net.ifmain.hwanultoktok.kmp.di.commonModule
import net.ifmain.hwanultoktok.kmp.di.platformModule
import net.ifmain.hwanultoktok.kmp.presentation.AppWithBottomAd
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HwanulTokTokApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HwanulTokTokApplication)
            modules(commonModule, platformModule)
        }

        ExchangeRateAlarmScheduler(this).ensureScheduled()
    }
}

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        maybeShowExactAlarmPermissionRationale()
    }
    private val exactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        ExchangeRateAlarmScheduler(this).scheduleNext()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // AdMob 초기화
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
        }
        
        try {
            setContent {
                AppWithAds()
            }
        } catch (e: Exception) {
            // DI 초기화 에러 발생 시 앱 재시작
            e.printStackTrace()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

        requestNotificationAndExactAlarmAccess()
    }

    private fun requestNotificationAndExactAlarmAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            maybeShowExactAlarmPermissionRationale()
        }
    }

    private fun maybeShowExactAlarmPermissionRationale() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val alarmScheduler = ExchangeRateAlarmScheduler(this)
        val preferences = getSharedPreferences(PERMISSION_PREFERENCES, MODE_PRIVATE)
        if (alarmScheduler.canScheduleExactAlarms() ||
            preferences.getBoolean(KEY_EXACT_ALARM_RATIONALE_SHOWN, false)
        ) {
            return
        }

        preferences.edit()
            .putBoolean(KEY_EXACT_ALARM_RATIONALE_SHOWN, true)
            .apply()

        android.app.AlertDialog.Builder(this)
            .setTitle("정확한 환율 알림 설정")
            .setMessage(
                "평일 오전 11시 30분에 환율을 확인하려면 " +
                    "'알람 및 리마인더' 권한이 필요합니다. " +
                    "허용하지 않아도 알림은 예약되지만 지연될 수 있습니다.",
            )
            .setPositiveButton("설정하기") { _, _ ->
                exactAlarmPermissionLauncher.launch(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:$packageName"),
                    ),
                )
            }
            .setNegativeButton("나중에", null)
            .show()
    }

    private companion object {
        const val PERMISSION_PREFERENCES = "notification_permission_guidance"
        const val KEY_EXACT_ALARM_RATIONALE_SHOWN = "exact_alarm_rationale_shown"
    }
}

@Composable
fun AppWithAds() {
    AppWithBottomAd()
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
